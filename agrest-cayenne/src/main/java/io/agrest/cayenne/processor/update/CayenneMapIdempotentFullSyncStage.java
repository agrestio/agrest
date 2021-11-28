package io.agrest.cayenne.processor.update;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.ResourceEntity;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.processor.CayenneUtil;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.SelectQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since 4.8
 */
public class CayenneMapIdempotentFullSyncStage extends CayenneMapIdempotentCreateOrUpdateStage {

    @Override
    protected <T extends DataObject> List<ChangeOperation<T>> map(UpdateContext<T> context) {

        ObjectMapper<T> mapper = createObjectMapper(context);

        List<ChangeOperation<T>> ops = new ArrayList<>(context.getUpdates().size());

        Map<Object, Collection<EntityUpdate<T>>> updatesByKey = mutableUpdatesByKey(context, mapper);

        List<T> allObjects = allObjects(context);

        for (T o : allObjects) {
            Object key = mapper.keyForObject(o);

            Collection<EntityUpdate<T>> updates = updatesByKey.remove(key);

            if (updates == null) {
                ops.add(new ChangeOperation<>(ChangeOperationType.DELETE, o, Collections.emptyList()));
            } else {
                ops.add(new ChangeOperation<>(ChangeOperationType.UPDATE, o, updates));
            }
        }

        processUnmapped(context, updatesByKey, ops);

        return ops;
    }

    <T extends DataObject> List<T> allObjects(UpdateContext<T> context) {

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
