package io.agrest.cayenne.processor.unrelate.stage;

import io.agrest.AgException;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.processor.CayenneUtil;
import io.agrest.id.AgObjectId;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.AgSchema;
import io.agrest.runtime.entity.IIdResolver;
import io.agrest.runtime.processor.unrelate.UnrelateContext;
import io.agrest.runtime.processor.unrelate.stage.UnrelateUpdateDataStoreStage;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.ObjEntity;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 2.7
 */
public class CayenneUnrelateDataStoreStage extends UnrelateUpdateDataStoreStage {

    private final AgSchema schema;
    private final IPathResolver pathResolver;

    public CayenneUnrelateDataStoreStage(
            @Inject IIdResolver idResolver,
            @Inject AgSchema schema,
            @Inject IPathResolver pathResolver) {

        super(idResolver);
        this.schema = schema;
        this.pathResolver = pathResolver;
    }

    @Override
    protected void unrelate(UnrelateContext<?> context) {
        ObjectContext cayenneContext = CayenneUnrelateStartStage.cayenneContext(context);

        if (context.getTargetId() != null) {
            unrelateSingle((UnrelateContext<Persistent>) context, cayenneContext);
        } else {
            unrelateAll((UnrelateContext<Persistent>) context, cayenneContext);
        }
    }

    private <T extends Persistent> void unrelateSingle(UnrelateContext<T> context, ObjectContext cayenneContext) {

        // validate relationship before doing anything else
        AgRelationship relationship = schema
                .getEntity(context.getType())
                .getRelationship(context.getRelationship());

        if (relationship == null) {
            throw AgException.badRequest("Invalid relationship: '%s'", context.getRelationship());
        }

        Persistent parent = (Persistent) getExistingObject(context.getType(), cayenneContext, context.getSourceId());

        Class<?> childType = relationship.getTargetEntity().getType();

        // among other things this call checks that the target exists
        Persistent child = (Persistent) getExistingObject(childType, cayenneContext, context.getTargetId());

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

    private <T extends Persistent> void unrelateAll(UnrelateContext<T> context, ObjectContext cayenneContext) {
        // validate relationship before doing anything else
        AgRelationship relationship = schema
                .getEntity(context.getType())
                .getRelationship(context.getRelationship());

        if (relationship == null) {
            throw AgException.badRequest("Invalid relationship: '%s'", context.getRelationship());
        }

        Persistent parent = (Persistent) getExistingObject(context.getType(), cayenneContext, context.getSourceId());

        if (relationship.isToMany()) {

            // clone relationship before we start deleting to avoid concurrent
            // modification of the iterator, and to be able to batch-delete
            // objects if needed
            @SuppressWarnings("unchecked")
            Collection<Persistent> relatedCollection = new ArrayList<>(
                    (Collection<Persistent>) parent.readProperty(relationship.getName()));

            for (Persistent o : relatedCollection) {
                parent.removeToManyTarget(relationship.getName(), o, true);
            }

        } else {

            Persistent target = (Persistent) parent.readProperty(relationship.getName());
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

        return CayenneUtil.findById(pathResolver, context, schema.getEntity(type), id);
    }
}
