package io.agrest.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.ResourceEntity;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.processor.CayenneUtil;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.SelectQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @since 4.8
 */
public class CayenneMapIdempotentFullSyncStage extends CayenneMapIdempotentCreateOrUpdateStage {

    @Override
    protected <T extends DataObject> void collectUpdateDeleteOps(
            UpdateContext<T> context, ObjectMapper<T> mapper,
            UpdateMap<T> updateMap) {

        List<T> existing = existingObjects(context, updateMap.getIds(), mapper);
        if (existing.isEmpty()) {
            return;
        }

        List<ChangeOperation<T>> updateOps = new ArrayList<>(updateMap.getIds().size());
        List<ChangeOperation<T>> deleteOps = new ArrayList<>();

        for (T o : existing) {
            Object key = mapper.keyForObject(o);

            EntityUpdate<T> updates = updateMap.remove(key);

            if (updates == null) {
                deleteOps.add(new ChangeOperation<>(ChangeOperationType.DELETE, o, null));
            } else {
                updateOps.add(new ChangeOperation<>(ChangeOperationType.UPDATE, o, updates));
            }
        }

        context.setChangeOperations(ChangeOperationType.UPDATE, updateOps);
        context.setChangeOperations(ChangeOperationType.DELETE, deleteOps);
    }

    @Override
    <T extends DataObject> List<T> existingObjects(UpdateContext<T> context, Collection<Object> keys, ObjectMapper<T> mapper) {

        buildQuery(context, context.getEntity(), null);

        // TODO: use SelectBuilder to get Cayenne representation of the resource, instead of duplicating this here...

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
