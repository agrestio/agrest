package io.agrest.runtime.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.ObjectMapperFactory;
import io.agrest.meta.AgEntity;
import io.agrest.runtime.cayenne.ByIdObjectMapperFactory;
import io.agrest.runtime.cayenne.converter.CayenneExpressionConverter;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        CayenneExpressionConverter expConverter = new CayenneExpressionConverter();

        List<Expression> expressions = new ArrayList<>(keys.size());
        for (Object key : keys) {

            Expression e = expConverter.convert(mapper.expressionForKey(key));
            if (e != null) {
                expressions.add(e);
            }
        }

        // no keys or all keys were for non-persistent objects
        if (expressions.isEmpty()) {
            return Collections.emptyList();
        }

        SelectQuery<T> query = SelectQuery.query(context.getType());
        query.setQualifier(ExpressionFactory.joinExp(Expression.OR, expressions));

        List<T> objects = CayenneUpdateStartStage.cayenneContext(context).select(query);
        if (context.isById() && objects.size() > 1) {
            throw new AgException(Response.Status.INTERNAL_SERVER_ERROR, String.format(
                    "Found more than one object for ID '%s' and entity '%s'",
                    context.getId(), context.getEntity().getAgEntity().getName()));
        }
        return objects;
    }
}
