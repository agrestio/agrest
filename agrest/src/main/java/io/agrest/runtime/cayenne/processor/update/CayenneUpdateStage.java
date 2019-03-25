package io.agrest.runtime.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.ObjectMapperFactory;
import io.agrest.ResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgPersistentEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.cayenne.CayenneAgRelationship;
import io.agrest.runtime.cayenne.ByIdObjectMapperFactory;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @since 2.7
 */
public class CayenneUpdateStage extends CayenneUpdateDataStoreStage {

    public CayenneUpdateStage(@Inject IMetadataService metadataService) {
        super(metadataService);
    }

    @Override
    protected <T extends DataObject> void sync(UpdateContext<T> context) {

        ObjectMapper<T> mapper = createObjectMapper(context);

        Map<Object, Collection<EntityUpdate<T>>> keyMap = mutableKeyMap(context, mapper);

        for (T o : itemsForKeys(context, keyMap.keySet(), mapper)) {
            Object key = mapper.keyForObject(o);

            Collection<EntityUpdate<T>> updates = keyMap.remove(key);

            // a null can only mean some algorithm malfunction
            if (updates == null) {
                throw new AgException(Response.Status.INTERNAL_SERVER_ERROR, "Invalid key item: " + key);
            }

            updateSingle(context, o, updates);
        }

        // check leftovers - those correspond to objects missing in the DB or
        // objects with no keys
        afterUpdatesMerge(context, keyMap);
    }

    protected <T extends DataObject> void afterUpdatesMerge(UpdateContext<T> context, Map<Object, Collection<EntityUpdate<T>>> keyMap) {
        if (!keyMap.isEmpty()) {
            Object firstKey = keyMap.keySet().iterator().next();

            if (firstKey == null) {
                throw new AgException(Response.Status.BAD_REQUEST, "Can't update. No id for object");
            }

            AgEntity<?> entity = context.getEntity().getAgEntity();
            throw new AgException(Response.Status.NOT_FOUND, "No object for ID '" + firstKey + "' and entity '"
                    + entity.getName() + "'");
        }
    }

    protected <T extends DataObject> Map<Object, Collection<EntityUpdate<T>>> mutableKeyMap(UpdateContext<T> context, ObjectMapper<T> mapper) {

        Collection<EntityUpdate<T>> updates = context.getUpdates();

        // sizing the map with one-update per key assumption
        Map<Object, Collection<EntityUpdate<T>>> map = new HashMap<>((int) (updates.size() / 0.75));

        for (EntityUpdate<T> u : updates) {

            Object key = mapper.keyForUpdate(u);
            Collection<EntityUpdate<T>> updatesForKey = map.get(key);
            if (updatesForKey == null) {
                updatesForKey = new ArrayList<>(2);
                map.put(key, updatesForKey);
            }

            updatesForKey.add(u);
        }

        return map;
    }

    protected <T extends DataObject> ObjectMapper<T> createObjectMapper(UpdateContext<T> context) {
        ObjectMapperFactory mapper = context.getMapper() != null ? context.getMapper() : ByIdObjectMapperFactory
                .mapper();
        return mapper.createMapper(context);
    }

    <T extends DataObject> List<T> itemsForKeys(UpdateContext<T> context, Collection<Object> keys, ObjectMapper<T> mapper) {

        // TODO: split query in batches:
        // respect Constants.SERVER_MAX_ID_QUALIFIER_SIZE_PROPERTY
        // property of Cayenne , breaking query into subqueries.
        // Otherwise this operation will not scale.. Though I guess since we are
        // not using streaming API to read data from Cayenne, we are already
        // limited in how much data can fit in the memory map.

        List<Expression> expressions = new ArrayList<>(keys.size());
        for (Object key : keys) {

            Expression e = mapper.expressionForKey(key);
            if (e != null) {
                expressions.add(e);
            }
        }

        // no keys or all keys were for non-persistent objects
        if (expressions.isEmpty()) {
            return Collections.emptyList();
        }

        ResourceEntity resourceEntity = context.getEntity();
        resourceEntity.setQualifier(ExpressionFactory.joinExp(Expression.OR, expressions));

        SelectQuery<T> query = buildQuery(context, context.getEntity());

        List<T> objects = fetchEntity(context, resourceEntity);
        if (context.isById() && objects.size() > 1) {
            throw new AgException(Response.Status.INTERNAL_SERVER_ERROR, String.format(
                    "Found more than one object for ID '%s' and entity '%s'",
                    context.getId(), context.getEntity().getAgEntity().getName()));
        }

        return objects;
    }


    <T> SelectQuery<T> buildQuery(UpdateContext<T> context, ResourceEntity<T> entity) {

        SelectQuery<T> query = SelectQuery.query(entity.getAgEntity().getType());

        // apply various request filters identifying the span of the collection

        if (entity.getQualifier() != null) {
            query.andQualifier(entity.getQualifier());
        }

        entity.setSelect(query);

        buildChildrenQuery(context, entity, entity.getChildren());

        return query;
    }

    protected void buildChildrenQuery(UpdateContext context, ResourceEntity<?> entity, Map<String, ResourceEntity<?>> children) {
        if (!children.isEmpty()) {
            for (Map.Entry<String, ResourceEntity<?>> e : children.entrySet()) {
                ResourceEntity child  = e.getValue();
                if (!(child.getAgEntity() instanceof AgPersistentEntity)) {
                    continue;
                }

                List<Property> properties = new ArrayList<>();
                properties.add(Property.createSelf(child.getType()));

                AgRelationship relationship = entity.getAgEntity().getRelationship(e.getKey());
                if (relationship != null && relationship instanceof CayenneAgRelationship) {
                    CayenneAgRelationship rel = (CayenneAgRelationship)relationship;
                    for (AgAttribute attribute : (Collection<AgAttribute>) entity.getAgEntity().getIds()) {
                        properties.add(Property.create(ExpressionFactory.dbPathExp(rel.getReverseDbPath() + "." + attribute.getName()), (Class) attribute.getType()));
                    }
                    // transfer expression from parent
                    if (entity.getSelect().getQualifier() != null) {
                        child.andQualifier((Expression) rel.translateExpressionToSource(entity.getSelect().getQualifier()));
                    }
                }

                SelectQuery childQuery = buildQuery(context, child);
                childQuery.setColumns(properties);
            }
        }
    }


    protected <T> List<T>  fetchEntity(UpdateContext<T> context, ResourceEntity<T> resourceEntity) {
        SelectQuery<T> select = resourceEntity.getSelect();

        List<T> objects = CayenneUpdateStartStage.cayenneContext(context).select(select);

        fetchChildren(context, resourceEntity, resourceEntity.getChildren());

        return objects;
    }

    protected <T> void fetchChildren(UpdateContext context, ResourceEntity<T> parent, Map<String, ResourceEntity<?>> children) {
        if (!children.isEmpty()) {
            for (Map.Entry<String, ResourceEntity<?>> e : children.entrySet()) {
                ResourceEntity childEntity = e.getValue();

                List childObjects = fetchEntity(context, childEntity);

                AgRelationship rel = parent.getAgEntity().getRelationship(e.getKey());

                assignChildrenToParent(
                        parent,
                        childObjects,
                        rel.isToMany()
                                ? (i, o) -> childEntity.addToManyResult(i, o)
                                : (i, o) -> childEntity.setToOneResult(i, o));
            }
        }
    }

    /**
     * Assigns child items to the appropriate parent item
     */
    protected <T> void assignChildrenToParent(ResourceEntity<T> parentEntity, List children, BiConsumer<AgObjectId, Object> resultKeeper) {
        // saves a result
        for (Object child : children) {
            if (child instanceof Object[]) {
                Object[] ids = (Object[])child;
                if (ids.length == 2) {
                    resultKeeper.accept(new SimpleObjectId(ids[1]), (T) ids[0]);
                } else if (ids.length > 2) {
                    // saves entity with a compound ID
                    Map<String, Object> compoundKeys = new LinkedHashMap<>();
                    AgAttribute[] idAttributes = parentEntity.getAgEntity().getIds().toArray(new AgAttribute[0]);
                    if (idAttributes.length == (ids.length - 1)) {
                        for (int i = 1; i < ids.length; i++) {
                            compoundKeys.put(idAttributes[i - 1].getName(), ids[i]);
                        }
                    }
                    resultKeeper.accept(new CompoundObjectId(compoundKeys), (T) ids[0]);
                }
            }
        }
    }
}
