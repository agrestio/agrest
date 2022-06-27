package io.agrest.jpa.pocessor.update.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.ObjectMapperFactory;
import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.id.AgObjectId;
import io.agrest.jpa.exp.IJpaExpParser;
import io.agrest.jpa.exp.JpaExpression;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.jpa.pocessor.JpaNestedResourceEntityExt;
import io.agrest.jpa.pocessor.JpaProcessor;
import io.agrest.jpa.query.JpaQueryBuilder;
import io.agrest.meta.AgAttribute;
import io.agrest.protocol.Exp;
import io.agrest.runtime.processor.update.ByIdObjectMapperFactory;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Metamodel;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaMapUpdateStage extends JpaMapChangesStage {

    private final IJpaExpParser qualifierParser;
    private final IJpaQueryAssembler queryAssembler;
    protected final Metamodel metamodel;

    public JpaMapUpdateStage(
            @Inject IJpaExpParser qualifierParser,
            @Inject IJpaQueryAssembler queryAssembler,
            @Inject IAgJpaPersister persister) {
        this.qualifierParser = qualifierParser;
        this.queryAssembler = queryAssembler;
        this.metamodel = persister.metamodel();
    }

    protected void map(UpdateContext<Object> context) {

        ObjectMapper<Object> mapper = createObjectMapper(context);

        UpdateMap<Object> updateMap = mutableUpdatesByKey(context, mapper);

        collectUpdateDeleteOps(context, mapper, updateMap);
        collectCreateOps(context, updateMap);
    }

    protected <T> void collectUpdateDeleteOps(
            UpdateContext<T> context,
            ObjectMapper<T> mapper,
            UpdateMap<T> updateMap) {

        List<T> existing = existingObjects(context, updateMap.getIds(), mapper);
        if (existing.isEmpty()) {
            return;
        }

        List<ChangeOperation<T>> updateOps = new ArrayList<>(existing.size());
        for (T o : existing) {
            Object key = mapper.keyForObject(o);
            EntityUpdate<T> update = updateMap.remove(key);

            // a null can only mean some algorithm malfunction
            if (update == null) {
                throw AgException.internalServerError("Invalid key item: %s", key);
            }

            updateOps.add(new ChangeOperation<>(ChangeOperationType.UPDATE, update.getEntity(), o, update));
        }

        context.setChangeOperations(ChangeOperationType.UPDATE, updateOps);
    }

    protected <T> void collectCreateOps(
            UpdateContext<T> context,
            UpdateMap<T> updateMap) {

        if (!updateMap.getNoId().isEmpty()) {
            throw AgException.badRequest("Request is not idempotent.");
        }

        if (!updateMap.getIds().isEmpty()) {
            throw AgException.notFound(
                    "No object for ID '%s' and entity '%s'",
                    updateMap.getIds().iterator().next(),
                    context.getEntity().getName());
        }
    }

    protected <T> ObjectMapper<T> createObjectMapper(UpdateContext<T> context) {
        ObjectMapperFactory mapper = context.getMapper() != null
                ? context.getMapper()
                : ByIdObjectMapperFactory.mapper();
        return mapper.createMapper(context);
    }

    protected <T> UpdateMap<T> mutableUpdatesByKey(
            UpdateContext<T> context,
            ObjectMapper<T> mapper) {

        Collection<EntityUpdate<T>> updates = context.getUpdates();

        // sizing the map with one-update per key assumption
        Map<Object, EntityUpdate<T>> withId = new HashMap<>((int) (updates.size() / 0.75));
        List<EntityUpdate<T>> noId = new ArrayList<>();

        for (EntityUpdate<T> u : updates) {
            Object key = mapper.keyForUpdate(u);

            // The key can be "null", and the update may still be valid. It means it won't match anything in the
            // DB though, and the request can not be idempotent...

            if (key == null) {
                noId.add(u);
            } else {
                withId.merge(key, u, EntityUpdate::merge);
            }
        }

        return new UpdateMap<>(withId, noId);
    }

    protected <T> List<T> existingObjects(
            UpdateContext<T> context,
            Collection<Object> keys,
            ObjectMapper<T> mapper) {

        if (keys.isEmpty()) {
            return Collections.emptyList();
        }

        JpaExpression rootQualifier = qualifierForKeys(keys, mapper);
        buildRootQuery(context, rootQualifier);

        List<T> objects = fetchRootEntity(context);

        if (context.isById() && objects.size() > 1) {
            throw AgException.internalServerError(
                    "Found more than one object for ID '%s' and entity '%s'",
                    context.getId(),
                    context.getEntity().getName());
        }

        return objects;
    }

    protected <T> List<T> fetchRootEntity(UpdateContext<T> context) {

        RootResourceEntity<T> entity = context.getEntity();
        JpaQueryBuilder rootQuery = JpaProcessor.getRootEntity(entity).getSelect();

        EntityManager entityManager = JpaUpdateStartStage.entityManager(context);
        @SuppressWarnings("unchecked")
        List<T> objects = rootQuery.build(entityManager).getResultList();
        for (RelatedResourceEntity<?> c : entity.getChildren().values()) {
            fetchNestedEntity(context, c);
        }

        return objects;
    }

    protected <T> void fetchNestedEntity(UpdateContext<T> context, RelatedResourceEntity<?> entity) {
        JpaNestedResourceEntityExt ext = JpaProcessor.getNestedEntity(entity);
        if (ext != null && ext.getSelect() != null) {
            EntityManager entityManager = JpaUpdateStartStage.entityManager(context);
            @SuppressWarnings("unchecked")
            List<Object[]> objects = ext.getSelect().build(entityManager).getResultList();

            assignChildrenToParent(entity, objects);

            for (RelatedResourceEntity<?> c : entity.getChildren().values()) {
                fetchNestedEntity(context, c);
            }
        }
    }

    private <T> void assignChildrenToParent(RelatedResourceEntity<T> entity, List<Object[]> objects) {
        ResourceEntity<?> parentEntity = entity.getParent();
        for (Object[] row : objects) {

            if (row.length == 2) {
                entity.addData(AgObjectId.of(row[1]), (T)row[0]);
            } else if (row.length > 2) {
                Map<String, Object> compoundKeys = new LinkedHashMap<>();
                AgAttribute[] idAttributes = parentEntity.getAgEntity().getIdParts().toArray(new AgAttribute[0]);
                if (idAttributes.length == (row.length - 1)) {
                    for (int i = 1; i < row.length; i++) {
                        compoundKeys.put(idAttributes[i - 1].getName(), row[i]);
                    }
                }
                entity.addData(AgObjectId.ofMap(compoundKeys), (T)row[0]);
            }
        }
    }

    protected <T> JpaQueryBuilder buildRootQuery(UpdateContext<T> context, JpaExpression rootQualifier) {
        // 1. build root query + nested queries
        JpaQueryBuilder rootQuery = JpaQueryBuilder.select("e").from(context.getEntity().getName(), "e")
                .where(rootQualifier);

        JpaProcessor.getRootEntity(context.getEntity()).setSelect(rootQuery);

        for (Map.Entry<String, RelatedResourceEntity<?>> e : context.getEntity().getChildren().entrySet()) {
            buildNestedQuery(e.getValue());
        }

        return rootQuery;
    }

    protected void buildNestedQuery(RelatedResourceEntity<?> entity) {
        JpaQueryBuilder query = queryAssembler.createQueryWithParentQualifier(entity);

        JpaProcessor.getNestedEntity(entity).setSelect(query);

        for (Map.Entry<String, RelatedResourceEntity<?>> e : entity.getChildren().entrySet()) {
            buildNestedQuery(e.getValue());
        }
    }

    protected JpaExpression qualifierForKeys(Collection<Object> keys, ObjectMapper<?> mapper) {
        JpaExpression expression = null;
        for(Object key: keys) {
            if (key != null) {
                Exp e = mapper.expressionForKey(key);
                if (e != null) {
                    JpaExpression parsedExp = qualifierParser.parse(e);
                    if(expression == null) {
                        expression = parsedExp;
                    } else {
                        expression = expression.or(parsedExp);
                    }
                }
            }
        }
        return expression;
    }
}
