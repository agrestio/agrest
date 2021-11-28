package io.agrest.cayenne.processor.update;

import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.EntityUpdate;
import io.agrest.NestedResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.ToManyResourceEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
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
public abstract class CayenneFillResponseStage implements Processor<UpdateContext<?>> {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute((UpdateContext<DataObject>) context);
        return ProcessorOutcome.CONTINUE;
    }

    protected abstract <T extends DataObject> int getHttpStatus(UpdateContext<T> context);

    @SuppressWarnings("unchecked")
    protected <T extends DataObject> void doExecute(UpdateContext<T> context) {

        context.setStatus(getHttpStatus(context));

        // response objects are attached to EntityUpdate instances ... if
        // 'includeData' is true create a list of unique updated objects in the
        // order corresponding to their initial appearance in the update.
        // We do not have to guarantee the order of objects in response (and
        // only Sencha seems to care - see #46), but there's not much overhead
        // involved, so we are doing it for all clients, not just Sencha

        if (context.isIncludingDataInResponse()) {

            // if there are dupes, the list size will be smaller... sizing it pessimistically
            List<T> objects = new ArrayList<>(context.getUpdates().size());

            // 'seen' is for a case of multiple updates per object in a request
            Set<ObjectId> seen = new HashSet<>();

            Map<String, NestedResourceEntity<?>> children = context.getEntity().getChildren();

            for (EntityUpdate<T> u : context.getUpdates()) {

                T o = (T) u.getMergedTo();
                if (o != null && seen.add(o.getObjectId())) {
                    objects.add(o);

                    // TODO: child entities should be seeded via a special NestedDataResolver to read from parent
                    //  instead of manually traversing objects
                    assignChildrenToParent(o, children);
                }
            }

            context.getEntity().setResult(objects);
        }
    }

    protected void assignChildrenToParent(DataObject root, Map<String, NestedResourceEntity<?>> children) {

        if (!children.isEmpty()) {
            for (Map.Entry<String, NestedResourceEntity<?>> e : children.entrySet()) {
                NestedResourceEntity childEntity = e.getValue();

                Object result = root.readPropertyDirectly(e.getKey());
                if (result == null || result instanceof Fault) {
                    continue;
                }

                AgObjectId id = root.getObjectId().getIdSnapshot().size() > 1
                        ? new CompoundObjectId(root.getObjectId().getIdSnapshot())
                        : new SimpleObjectId(root.getObjectId().getIdSnapshot().values().iterator().next());

                if (childEntity instanceof ToManyResourceEntity) {
                    List r = (List) result;

                    ((ToManyResourceEntity) childEntity).addResultList(id, r);
                    for (Object ro : r) {
                        assignChildrenToParent((DataObject) ro, childEntity.getChildren());
                    }

                } else {
                    childEntity.addResult(id, result);
                    assignChildrenToParent((DataObject) result, childEntity.getChildren());
                }
            }
        }
    }
}
