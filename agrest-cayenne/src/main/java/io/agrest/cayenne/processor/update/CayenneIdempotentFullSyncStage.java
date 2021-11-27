package io.agrest.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.ResourceEntity;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.processor.CayenneUtil;
import io.agrest.meta.AgDataMap;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.SelectQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class CayenneIdempotentFullSyncStage extends CayenneIdempotentCreateOrUpdateStage {

    public CayenneIdempotentFullSyncStage(
            @Inject AgDataMap dataMap,
            @Inject ICayennePersister persister) {
        super(dataMap, persister);
    }

    @Override
    protected <T extends DataObject> void sync(UpdateContext<T> context) {

        ObjectRelator relator = createRelator(context);
        ObjectMapper<T> mapper = createObjectMapper(context);
        Map<Object, Collection<EntityUpdate<T>>> keyMap = mutableUpdatesByKey(context, mapper);

        List<T> allObjects = allItems(context);

        List<DataObject> deletedObjects = new ArrayList<>();

        for (T o : allObjects) {
            Object key = mapper.keyForObject(o);

            Collection<EntityUpdate<T>> updates = keyMap.remove(key);

            if (updates == null) {
                deletedObjects.add(o);
            } else {
                updateSingle(relator, o, updates);
            }
        }

        if (!deletedObjects.isEmpty()) {
            CayenneUpdateStartStage.cayenneContext(context).deleteObjects(deletedObjects);
        }

        // check leftovers - those correspond to objects missing in the DB or objects with no keys
        afterUpdatesMerge(context, keyMap);
    }

    <T extends DataObject> List<T> allItems(UpdateContext<T> context) {

        buildQuery(context, context.getEntity(), null);

        // TODO: use SelectBuilder to get Cayenne representation of the
        // resource, instead of duplicating this here...

        // List<T> objects = CayenneUpdateStartStage.cayenneContext(context).select(query);
        List<T> objects = fetchEntity(context, context.getEntity());

        if (context.isById() && objects.size() > 1) {
            throw AgException.internalServerError(
                    "Found more than one object for ID '%s' and entity '%s'",
                    context.getId(),
                    context.getEntity().getName());
        }
        return objects;
    }

    @Override
    <T> SelectQuery<T> buildQuery(UpdateContext<T> context, ResourceEntity<T> entity, Expression qualifier) {

        SelectQuery<T> query = SelectQuery.query(entity.getType());

        if (qualifier != null) {
            query.andQualifier(qualifier);
        }

        if (context.getParent() != null) {
            EntityResolver resolver = CayenneUpdateStartStage.cayenneContext(context).getEntityResolver();
            query.andQualifier(CayenneUtil.parentQualifier(context.getParent(), resolver));
        }

        CayenneProcessor.setQuery(entity, query);
        buildChildrenQuery(context, entity);

        return query;
    }

}
