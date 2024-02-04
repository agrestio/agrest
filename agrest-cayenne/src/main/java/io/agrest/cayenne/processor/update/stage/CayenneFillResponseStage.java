package io.agrest.cayenne.processor.update.stage;

import io.agrest.EntityUpdate;
import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.ToManyResourceEntity;
import io.agrest.id.AgObjectId;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.reader.DataReader;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.stage.UpdateFillResponseStage;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.Fault;
import org.apache.cayenne.ObjectId;

import java.util.ArrayList;
import java.util.Collection;
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

        context.setResponseStatus(getHttpStatus(context));

        if (context.isIncludingDataInResponse()) {

            // Updated objects are attached to EntityUpdate instances ... Create a list of unique updated
            // objects in the order corresponding to their initial appearance in the updates collection.

            // if there are dupes, the list size will be smaller... sizing it pessimistically
            List<T> objects = new ArrayList<>(context.getUpdates().size());

            // 'seen' is for a case of multiple updates per object in a request
            Set<ObjectId> seen = new HashSet<>();

            for (EntityUpdate<T> u : context.getUpdates()) {

                T o = (T) u.getTargetObject();
                if (o != null && seen.add(o.getObjectId())) {
                    objects.add(o);

                    // TODO: child entities should be seeded via a special RelatedDataResolver to read from parent
                    //  instead of manually traversing objects
                    assignChildrenToParent(o, context.getEntity());
                }
            }

            context.getEntity().setData(objects);
        }
    }

    protected void assignChildrenToParent(DataObject root, ResourceEntity<?> entity) {

        DataReader idReader = entity.getAgEntity().getIdReader();
        Collection<RelatedResourceEntity<?>> children = entity.getChildren();

        if (!children.isEmpty()) {

            for (RelatedResourceEntity childEntity : children) {

                Object result = root.readPropertyDirectly(childEntity.getIncoming().getName());
                if (result == null || result instanceof Fault) {
                    continue;
                }

                // TODO: getIdSnapshot() will not prefix keys with "db". Must use AgIdParts to resolve the ID
                Map<String, Object> idMap = (Map<String, Object>) idReader.read(root);
                AgObjectId id = idMap.size() > 1
                        ? AgObjectId.ofMap(idMap)
                        : AgObjectId.of(idMap.values().iterator().next());

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
