package io.agrest.cayenne.processor.update;

import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.EntityUpdate;
import io.agrest.NestedResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.ToManyResourceEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.Fault;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles {@link io.agrest.UpdateStage#COMMIT} stage of the update process.
 *
 * @since 3.6
 */
public class CayenneCommitStage implements Processor<UpdateContext<?>> {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        CayenneUpdateStartStage.cayenneContext(context).commitChanges();

        // Store parent-child result list in ResourceEntity
        // TODO: Replace this with a dedicated child mapping stage..

        RootResourceEntity entity = context.getEntity();
        Map<String, NestedResourceEntity<?>> children = entity.getChildren();
        List rootResult = new ArrayList();
        for (EntityUpdate<?> u : context.getUpdates()) {
            DataObject o = (DataObject) u.getMergedTo();
            rootResult.add(o);
            assignChildrenToParent(o, children);
        }

        entity.setResult(rootResult);
        return ProcessorOutcome.CONTINUE;
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
