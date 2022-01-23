package io.agrest.cayenne.processor.update;

import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.EntityUpdate;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.ToManyResourceEntity;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.property.PropertyReader;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.stage.UpdateFillResponseStage;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.Fault;
import org.apache.cayenne.ObjectId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since 2.7
 */
public abstract class CayenneFillResponseStage extends UpdateFillResponseStage {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute((UpdateContext<DataObject>) context);
        return ProcessorOutcome.CONTINUE;
    }

    protected abstract <T extends DataObject> int getHttpStatus(UpdateContext<T> context);

    @SuppressWarnings("unchecked")
    protected <T extends DataObject> void doExecute(UpdateContext<T> context) {

        context.setStatus(getHttpStatus(context));

        if (context.isIncludingDataInResponse()) {

            // Updated objects are attached to EntityUpdate instances ... Create a list of unique updated
            // objects in the order corresponding to their initial appearance in the updates collection.

            // if there are dupes, the list size will be smaller... sizing it pessimistically
            List<T> objects = new ArrayList<>(context.getUpdates().size());

            // 'seen' is for a case of multiple updates per object in a request
            Set<ObjectId> seen = new HashSet<>();

            for (EntityUpdate<T> u : context.getUpdates()) {

                T o = (T) u.getMergedTo();
                if (o != null && seen.add(o.getObjectId())) {
                    objects.add(o);

                    // TODO: child entities should be seeded via a special NestedDataResolver to read from parent
                    //  instead of manually traversing objects
                    assignChildrenToParent(o, context.getEntity());
                }
            }

            context.getEntity().setData(objects);
        }
    }

    protected void assignChildrenToParent(DataObject root, ResourceEntity<?> entity) {

        PropertyReader idReader = entity.getAgEntity().getIdReader();
        Map<String, NestedResourceEntity<?>> children = entity.getChildren();

        if (!children.isEmpty()) {

            for (Map.Entry<String, NestedResourceEntity<?>> e : children.entrySet()) {
                NestedResourceEntity childEntity = e.getValue();

                Object result = root.readPropertyDirectly(e.getKey());
                if (result == null || result instanceof Fault) {
                    continue;
                }

                // TODO: getIdSnapshot() will not prefix keys with "db". Must use AgIdParts to resolve the ID
                Map<String, Object> idMap = (Map<String, Object>) idReader.value(root);
                AgObjectId id = idMap.size() > 1
                        ? new CompoundObjectId(idMap)
                        : new SimpleObjectId(idMap.values().iterator().next());

                if (childEntity instanceof ToManyResourceEntity) {
                    List r = (List) result;

                    ((ToManyResourceEntity) childEntity).setData(id, r);
                    for (Object ro : r) {
                        assignChildrenToParent((DataObject) ro, childEntity);
                    }

                } else {
                    childEntity.addData(id, result);
                    assignChildrenToParent((DataObject) result, childEntity);
                }
            }
        }
    }
}
