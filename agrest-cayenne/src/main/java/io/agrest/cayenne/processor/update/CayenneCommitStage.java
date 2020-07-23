package io.agrest.cayenne.processor.update;

import io.agrest.*;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneUtil;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.*;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.*;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.reflect.ClassDescriptor;

import javax.ws.rs.core.Response;
import java.util.*;

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
            assignChildrenToParent(o, entity, children);
        }

        entity.setResult(rootResult);
        return ProcessorOutcome.CONTINUE;
    }

    protected void assignChildrenToParent(DataObject root, ResourceEntity<?> parent, Map<String, NestedResourceEntity<?>> children) {
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

                AgRelationship rel = parent.getChild(e.getKey()).getIncoming();
                if (rel.isToMany() && result instanceof List) {
                    List r = (List) result;

                    childEntity.setToManyResult(id, r);
                    for (Object ro : r) {
                        assignChildrenToParent((DataObject) ro, childEntity, childEntity.getChildren());
                    }

                } else {
                    childEntity.setToOneResult(id, result);
                    assignChildrenToParent((DataObject) result, childEntity, childEntity.getChildren());
                }
            }
        }
    }
}
