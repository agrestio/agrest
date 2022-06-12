package io.agrest.cayenne.processor.unrelate.stage;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.processor.CayenneUtil;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.AgSchema;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.unrelate.UnrelateContext;
import io.agrest.runtime.processor.unrelate.stage.UnrelateUpdateDateStoreStage;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.ObjEntity;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 2.7
 */
public class CayenneUnrelateDataStoreStage extends UnrelateUpdateDateStoreStage {

    private final AgSchema schema;
    private final IPathResolver pathResolver;

    public CayenneUnrelateDataStoreStage(
            @Inject AgSchema schema,
            @Inject IPathResolver pathResolver) {
        this.schema = schema;
        this.pathResolver = pathResolver;
    }

    @Override
    public ProcessorOutcome execute(UnrelateContext<?> context) {
        doExecute((UnrelateContext<DataObject>) context);
        return ProcessorOutcome.CONTINUE;
    }


    protected <T extends DataObject> void doExecute(UnrelateContext<T> context) {

        ObjectContext cayenneContext = CayenneUnrelateStartStage.cayenneContext(context);

        if (context.getTargetId() != null) {
            unrelateSingle(context, cayenneContext);
        } else {
            unrelateAll(context, cayenneContext);
        }
    }

    private <T extends DataObject> void unrelateSingle(UnrelateContext<T> context, ObjectContext cayenneContext) {

        // validate relationship before doing anything else
        AgRelationship relationship = schema
                .getEntity(context.getType())
                .getRelationship(context.getRelationship());

        if (relationship == null) {
            throw AgException.badRequest("Invalid relationship: '%s'", context.getRelationship());
        }

        DataObject parent = (DataObject) getExistingObject(context.getType(), cayenneContext, context.getSourceId());

        Class<?> childType = relationship.getTargetEntity().getType();

        // among other things this call checks that the target exists
        DataObject child = (DataObject) getExistingObject(childType, cayenneContext, context.getTargetId());

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
        AgRelationship relationship = schema
                .getEntity(context.getType())
                .getRelationship(context.getRelationship());

        if (relationship == null) {
            throw AgException.badRequest("Invalid relationship: '%s'", context.getRelationship());
        }

        DataObject parent = (DataObject) getExistingObject(context.getType(), cayenneContext, context.getSourceId());

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

    private Object getExistingObject(Class<?> type, ObjectContext context, AgObjectId id) {

        Object object = getOptionalExistingObject(type, context, id);
        if (object == null) {
            ObjEntity entity = context.getEntityResolver().getObjEntity(type);
            throw AgException.notFound("No object for ID '%s' and entity '%s'", id, entity.getName());
        }

        return object;
    }

    private Object getOptionalExistingObject(Class<?> type, ObjectContext context, AgObjectId id) {

        ObjEntity entity = context.getEntityResolver().getObjEntity(type);

        // sanity checking...
        if (entity == null) {
            throw AgException.internalServerError("Unknown entity class: %s", type);
        }

        // TODO: should we start using optimistic locking on PK by default instead of SELECT/DELETE|UPDATE?

        return CayenneUtil.findById(pathResolver, context, type, schema.getEntity(type), id);
    }
}
