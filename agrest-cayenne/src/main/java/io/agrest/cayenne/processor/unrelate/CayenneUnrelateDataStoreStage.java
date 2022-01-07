package io.agrest.cayenne.processor.unrelate;

import io.agrest.AgException;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.unrelate.UnrelateContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.ObjectIdQuery;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 2.7
 */
public class CayenneUnrelateDataStoreStage implements Processor<UnrelateContext<?>> {

    private final AgDataMap dataMap;

    public CayenneUnrelateDataStoreStage(@Inject AgDataMap dataMap) {
        this.dataMap = dataMap;
    }

    @Override
    public ProcessorOutcome execute(UnrelateContext<?> context) {
        doExecute((UnrelateContext<DataObject>) context);
        return ProcessorOutcome.CONTINUE;
    }


    protected <T extends DataObject> void doExecute(UnrelateContext<T> context) {

        ObjectContext cayenneContext = CayenneUnrelateStartStage.cayenneContext(context);

        if (context.getId() != null) {
            unrelateSingle(context, cayenneContext);
        } else {
            unrelateAll(context, cayenneContext);
        }
    }

    private <T extends DataObject> void unrelateSingle(UnrelateContext<T> context, ObjectContext cayenneContext) {

        // validate relationship before doing anything else
        AgRelationship relationship = dataMap
                .getEntity(context.getParent().getType())
                .getRelationship(context.getParent().getRelationship());

        if (relationship == null) {
            throw AgException.badRequest("Invalid relationship: '%s'", context.getParent().getRelationship());
        }

        // TODO: #521 use CayenneUtil.parentQualifier(..)
        DataObject parent = (DataObject) getExistingObject(context.getParent().getType(), cayenneContext, context
                .getParent().getId().get());

        Class<?> childType = relationship.getTargetEntity().getType();

        // TODO: #521 use CayenneUtil.findById(..)
        // among other things this call checks that the target exists
        DataObject child = (DataObject) getExistingObject(childType, cayenneContext, context.getId());

        if (relationship.isToMany()) {

            // sanity check...
            Collection<?> relatedCollection = (Collection<?>) parent.readProperty(relationship.getName());
            if (!relatedCollection.contains(child)) {
                throw AgException.badRequest("Source and target are not related");
            }

            parent.removeToManyTarget(relationship.getName(), child, true);
        } else {

            // sanity check...
            if (parent.readProperty(relationship.getName()) != child) {
                throw AgException.badRequest("Source and target are not related");
            }

            parent.setToOneTarget(relationship.getName(), null, true);
        }

        cayenneContext.commitChanges();
    }

    private <T extends DataObject> void unrelateAll(UnrelateContext<T> context, ObjectContext cayenneContext) {
        // validate relationship before doing anything else
        AgRelationship relationship = dataMap
                .getEntity(context.getParent().getType())
                .getRelationship(context.getParent().getRelationship());

        if (relationship == null) {
            throw AgException.badRequest("Invalid relationship: '%s'", context.getParent().getRelationship());
        }

        // TODO: #521 use CayenneUtil.parentQualifier(..)
        DataObject parent = (DataObject) getExistingObject(context.getParent().getType(), cayenneContext, context
                .getParent().getId().get());

        if (relationship.isToMany()) {

            // clone relationship before we start deleting to avoid concurrent
            // modification of the iterator, and to be able to batch-delete
            // objects if needed
            @SuppressWarnings("unchecked")
            Collection<DataObject> relatedCollection = new ArrayList<>(
                    (Collection<DataObject>) parent.readProperty(relationship.getName()));

            for (DataObject o : relatedCollection) {
                parent.removeToManyTarget(relationship.getName(), o, true);
            }

        } else {

            DataObject target = (DataObject) parent.readProperty(relationship.getName());
            if (target != null) {
                parent.setToOneTarget(relationship.getName(), null, true);
            }
        }

        cayenneContext.commitChanges();
    }

    // TODO: use CayenneUtil.findById(..) or CayenneUtil.parentQualifier()
    private Object getExistingObject(Class<?> type, ObjectContext context, Object id) {

        Object object = getOptionalExistingObject(type, context, id);
        if (object == null) {
            ObjEntity entity = context.getEntityResolver().getObjEntity(type);
            throw AgException.notFound("No object for ID '%s' and entity '%s'", id, entity.getName());
        }

        return object;
    }

    // TODO: use CayenneUtil.findById(..) or CayenneUtil.parentQualifier
    private Object getOptionalExistingObject(Class<?> type, ObjectContext context, Object id) {

        ObjEntity entity = context.getEntityResolver().getObjEntity(type);

        // sanity checking...
        if (entity == null) {
            throw AgException.internalServerError("Unknown entity class: %s", type);
        }

        // TODO: should we start using optimistic locking on PK by default
        // instead of SELECT/DELETE|UPDATE?

        String idName = entity.getPrimaryKeyNames().iterator().next();
        ObjectIdQuery select = new ObjectIdQuery(ObjectId.of(entity.getName(), idName, id));

        return Cayenne.objectForQuery(context, select);
    }
}
