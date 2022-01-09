package io.agrest.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.CompoundObjectId;
import io.agrest.EntityUpdate;
import io.agrest.NestedResourceEntity;
import io.agrest.ObjectMapper;
import io.agrest.ObjectMapperFactory;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.base.protocol.Exp;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.processor.CayenneResourceEntityExt;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.cayenne.qualifier.IQualifierParser;
import io.agrest.meta.AgAttribute;
import io.agrest.runtime.processor.update.ByIdObjectMapperFactory;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.SelectQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 4.8
 */
public class CayenneMapUpdateStage extends CayenneMapChangesStage {

    private final IQualifierParser qualifierParser;
    private final ICayenneQueryAssembler queryAssembler;
    protected final EntityResolver entityResolver;

    public CayenneMapUpdateStage(
            @Inject IQualifierParser qualifierParser,
            @Inject ICayenneQueryAssembler queryAssembler,
            @Inject ICayennePersister persister) {
        this.qualifierParser = qualifierParser;
        this.queryAssembler = queryAssembler;
        this.entityResolver = persister.entityResolver();
    }

    protected <T extends DataObject> void map(UpdateContext<T> context) {

        ObjectMapper<T> mapper = createObjectMapper(context);

        UpdateMap<T> updateMap = mutableUpdatesByKey(context, mapper);

        collectUpdateDeleteOps(context, mapper, updateMap);
        collectCreateOps(context, updateMap);
    }

    protected <T extends DataObject> void collectUpdateDeleteOps(
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

    protected <T extends DataObject> void collectCreateOps(
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

    protected <T extends DataObject> ObjectMapper<T> createObjectMapper(UpdateContext<T> context) {
        ObjectMapperFactory mapper = context.getMapper() != null
                ? context.getMapper()
                : ByIdObjectMapperFactory.mapper();
        return mapper.createMapper(context);
    }

    protected <T extends DataObject> UpdateMap<T> mutableUpdatesByKey(
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
                withId.merge(key, u, (oldVal, newVal) -> oldVal.merge(newVal));
            }
        }

        return new UpdateMap<>(withId, noId);
    }

    protected <T extends DataObject> List<T> existingObjects(
            UpdateContext<T> context,
            Collection<Object> keys,
            ObjectMapper<T> mapper) {

        if (keys.isEmpty()) {
            return Collections.emptyList();
        }

        // TODO: split query in batches:
        // respect Constants.SERVER_MAX_ID_QUALIFIER_SIZE_PROPERTY
        // property of Cayenne , breaking query into subqueries.
        // Otherwise this operation will not scale.. Though I guess since we are
        // not using streaming API to read data from Cayenne, we are already
        // limited in how much data can fit in the memory map.

        Expression rootQualifier = qualifierForKeys(keys, mapper);
        if (rootQualifier == null) {
            return Collections.emptyList();
        }

        buildRootQuery(context.getEntity(), rootQualifier);

        // TODO: implement entity-tied resolvers for updates to avoid duplicating selecting logic

        List<T> objects = fetchRootEntity(CayenneUpdateStartStage.cayenneContext(context), context.getEntity());
        if (context.isById() && objects.size() > 1) {
            throw AgException.internalServerError(
                    "Found more than one object for ID '%s' and entity '%s'",
                    context.getId(),
                    context.getEntity().getName());
        }

        return objects;
    }

    protected Expression qualifierForKeys(Collection<Object> keys, ObjectMapper<?> mapper) {
        List<Expression> expressions = new ArrayList<>(keys.size());
        for (Object key : keys) {
            // update keys can be null... see a note in "mutableUpdatesByKey"
            if (key != null) {
                Exp e = mapper.expressionForKey(key);
                if (e != null) {
                    expressions.add(qualifierParser.parse(e));
                }
            }
        }

        return expressions.isEmpty() ? null : ExpressionFactory.or(expressions);
    }

    protected <T> SelectQuery<T> buildRootQuery(RootResourceEntity<T> entity, Expression qualifier) {

        SelectQuery<T> query = SelectQuery.query(entity.getType());
        query.setQualifier(qualifier);

        for (Map.Entry<String, NestedResourceEntity<?>> e : entity.getChildren().entrySet()) {
            buildNestedQuery(e.getValue(), query.getQualifier());
        }

        CayenneProcessor.getCayenneEntity(entity).setSelect(query);
        return query;
    }

    protected <T> SelectQuery<T> buildNestedQuery(
            NestedResourceEntity<T> entity,
            Expression parentQualifier) {

        ObjEntity parentObjEntity = entityResolver.getObjEntity(entity.getParent().getName());
        ObjRelationship incomingObjRelationship = parentObjEntity.getRelationship(entity.getIncoming().getName());

        // relationship may not be mapped in Cayenne...
        if (incomingObjRelationship == null) {
            return null;
        }

        SelectQuery<T> query = SelectQuery.query(entity.getType());
        query.setQualifier(translateExpressionToSource(incomingObjRelationship, parentQualifier));
        query.setColumns(queryAssembler.queryColumns(entity));

        for (Map.Entry<String, NestedResourceEntity<?>> e : entity.getChildren().entrySet()) {
            buildNestedQuery(e.getValue(), query.getQualifier());
        }

        CayenneProcessor.getCayenneEntity(entity).setSelect(query);
        return query;
    }

    protected <T> List<T> fetchRootEntity(ObjectContext context, RootResourceEntity<T> entity) {

        List<T> objects = CayenneProcessor.getCayenneEntity(entity).getSelect().select(context);
        for (NestedResourceEntity<?> c : entity.getChildren().values()) {
            fetchNestedEntity(context, c);
        }

        return objects;
    }

    protected <T> void fetchNestedEntity(ObjectContext context, NestedResourceEntity<T> entity) {

        CayenneResourceEntityExt<T> ext = CayenneProcessor.getCayenneEntity(entity);
        if (ext != null && ext.getSelect() != null) {
            List<T> objects = ext.getSelect().select(context);
            assignChildrenToParent(entity, objects);

            for (NestedResourceEntity<?> c : entity.getChildren().values()) {
                fetchNestedEntity(context, c);
            }
        }
    }

    protected <T> void assignChildrenToParent(NestedResourceEntity<T> entity, List<T> objects) {

        ResourceEntity<?> parentEntity = entity.getParent();

        for (Object child : objects) {
            if (child instanceof Object[]) {
                Object[] ids = (Object[]) child;
                if (ids.length == 2) {
                    entity.addResult(new SimpleObjectId(ids[1]), (T) ids[0]);
                } else if (ids.length > 2) {
                    // saves entity with a compound ID
                    Map<String, Object> compoundKeys = new LinkedHashMap<>();
                    AgAttribute[] idAttributes = parentEntity.getAgEntity().getIdParts().toArray(new AgAttribute[0]);
                    if (idAttributes.length == (ids.length - 1)) {
                        for (int i = 1; i < ids.length; i++) {
                            compoundKeys.put(idAttributes[i - 1].getName(), ids[i]);
                        }
                    }
                    entity.addResult(new CompoundObjectId(compoundKeys), (T) ids[0]);
                }
            }
        }
    }

    protected Expression translateExpressionToSource(ObjRelationship relationship, Expression expression) {
        return expression != null
                ? relationship.getSourceEntity().translateToRelatedEntity(expression, relationship.getName())
                : null;
    }
}
