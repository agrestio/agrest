package io.agrest.runtime.cayenne.processor.unrelate;

import io.agrest.LinkRestException;
import io.agrest.meta.LrRelationship;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.unrelate.UnrelateContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.ObjectIdQuery;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 2.7
 */
public class CayenneUnrelateDataStoreStage implements Processor<UnrelateContext<?>> {

    private IMetadataService metadataService;

    public CayenneUnrelateDataStoreStage(@Inject IMetadataService metadataService) {
        this.metadataService = metadataService;
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
        LrRelationship relationship = metadataService.getLrRelationship(context.getParent());

        DataObject parent = (DataObject) getExistingObject(context.getParent().getType(), cayenneContext, context
                .getParent().getId().get());

        Class<?> childType = relationship.getTargetEntity().getType();

        // among other things this call checks that the target exists
        DataObject child = (DataObject) getExistingObject(childType, cayenneContext, context.getId());

        if (relationship.isToMany()) {

            // sanity check...
            Collection<?> relatedCollection = (Collection<?>) parent.readProperty(relationship.getName());
            if (!relatedCollection.contains(child)) {
                throw new LinkRestException(Response.Status.EXPECTATION_FAILED, "Source and target are not related");
            }

            parent.removeToManyTarget(relationship.getName(), child, true);
        } else {

            // sanity check...
            if (parent.readProperty(relationship.getName()) != child) {
                throw new LinkRestException(Response.Status.EXPECTATION_FAILED, "Source and target are not related");
            }

            parent.setToOneTarget(relationship.getName(), null, true);
        }

        cayenneContext.commitChanges();
    }

    private <T extends DataObject> void unrelateAll(UnrelateContext<T> context, ObjectContext cayenneContext) {
        // validate relationship before doing anything else
        LrRelationship lrRelationship = metadataService.getLrRelationship(context.getParent());

        DataObject parent = (DataObject) getExistingObject(context.getParent().getType(), cayenneContext, context
                .getParent().getId().get());

        if (lrRelationship.isToMany()) {

            // clone relationship before we start deleting to avoid concurrent
            // modification of the iterator, and to be able to batch-delete
            // objects if needed
            @SuppressWarnings("unchecked")
            Collection<DataObject> relatedCollection = new ArrayList<>(
                    (Collection<DataObject>) parent.readProperty(lrRelationship.getName()));

            for (DataObject o : relatedCollection) {
                parent.removeToManyTarget(lrRelationship.getName(), o, true);
            }

        } else {

            DataObject target = (DataObject) parent.readProperty(lrRelationship.getName());
            if (target != null) {
                parent.setToOneTarget(lrRelationship.getName(), null, true);
            }
        }

        cayenneContext.commitChanges();
    }

    // TODO: use ObjectMapper
    private Object getExistingObject(Class<?> type, ObjectContext context, Object id) {

        Object object = getOptionalExistingObject(type, context, id);
        if (object == null) {
            ObjEntity entity = context.getEntityResolver().getObjEntity(type);
            throw new LinkRestException(Response.Status.NOT_FOUND, "No object for ID '" + id + "' and entity '"
                    + entity.getName() + "'");
        }

        return object;
    }

    // TODO: use ObjectMapper
    @SuppressWarnings("unchecked")
    private Object getOptionalExistingObject(Class<?> type, ObjectContext context, Object id) {

        ObjEntity entity = context.getEntityResolver().getObjEntity(type);

        // sanity checking...
        if (entity == null) {
            throw new LinkRestException(Response.Status.INTERNAL_SERVER_ERROR, "Unknown entity class: " + type);
        }

        // TODO: should we start using optimistic locking on PK by default
        // instead of SELECT/DELETE|UPDATE?

        String idName = entity.getPrimaryKeyNames().iterator().next();
        ObjectIdQuery select = new ObjectIdQuery(new ObjectId(entity.getName(), idName, id));

        return Cayenne.objectForQuery(context, select);
    }
}
