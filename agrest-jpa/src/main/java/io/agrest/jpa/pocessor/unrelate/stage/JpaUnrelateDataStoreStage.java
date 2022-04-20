package io.agrest.jpa.pocessor.unrelate.stage;

import java.util.ArrayList;
import java.util.Collection;

import io.agrest.AgException;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.JpaUtil;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.unrelate.UnrelateContext;
import io.agrest.runtime.processor.unrelate.stage.UnrelateUpdateDateStoreStage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Metamodel;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaUnrelateDataStoreStage extends UnrelateUpdateDateStoreStage {

    private final AgDataMap dataMap;
    private final Metamodel metamodel;

    public JpaUnrelateDataStoreStage(@Inject AgDataMap dataMap, @Inject IAgJpaPersister persister) {
        this.dataMap = dataMap;
        this.metamodel = persister.metamodel();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ProcessorOutcome execute(UnrelateContext<?> context) {
        doExecute((UnrelateContext<Object>) context);
        return ProcessorOutcome.CONTINUE;
    }

    protected void doExecute(UnrelateContext<Object> context) {
        EntityManager entityManager = JpaUnrelateStartStage.entityManager(context);
        entityManager.getTransaction().begin();
        if (context.getTargetId() != null) {
            unrelateSingle(context, entityManager);
        } else {
            unrelateAll(context, entityManager);
        }
        entityManager.getTransaction().commit();
    }

    private void unrelateSingle(UnrelateContext<Object> context, EntityManager entityManager) {

        // validate relationship before doing anything else
        AgRelationship relationship = dataMap
                .getEntity(context.getType())
                .getRelationship(context.getRelationship());

        if (relationship == null) {
            throw AgException.badRequest("Invalid relationship: '%s'", context.getRelationship());
        }

        Object parent = getExistingObject(context.getType(), entityManager, context.getSourceId());

        Class<?> childType = relationship.getTargetEntity().getType();

        // among other things this call checks that the target exists
        Object child = getExistingObject(childType, entityManager, context.getTargetId());
        Attribute<?,?> attribute = metamodel.entity(context.getType()).getAttribute(relationship.getName());

        if (relationship.isToMany()) {
            // sanity check...
            Collection<?> relatedCollection = (Collection<?>) JpaUtil.readProperty(parent, attribute);
            if (!relatedCollection.contains(child)) {
                throw AgException.badRequest("Source and target are not related");
            }
            relatedCollection.remove(child);
        } else {
            // sanity check...
            if (JpaUtil.readProperty(parent, attribute) != child) {
                throw AgException.badRequest("Source and target are not related");
            }
            JpaUtil.writeProperty(parent, attribute, null);
        }
    }

    private void unrelateAll(UnrelateContext<Object> context, EntityManager entityManager) {
        // validate relationship before doing anything else
        AgRelationship relationship = dataMap
                .getEntity(context.getType())
                .getRelationship(context.getRelationship());

        if (relationship == null) {
            throw AgException.badRequest("Invalid relationship: '%s'", context.getRelationship());
        }

        Object parent = getExistingObject(context.getType(), entityManager, context.getSourceId());
        Attribute<?,?> attribute = metamodel.entity(context.getType()).getAttribute(relationship.getName());

        if (relationship.isToMany()) {
            // clone relationship before we start deleting to avoid concurrent
            // modification of the iterator, and to be able to batch-delete
            // objects if needed
            Collection<?> objects = (Collection<?>) JpaUtil.readProperty(parent, attribute);
            Collection<Object> relatedCollection = new ArrayList<>(objects);
            for (Object o : relatedCollection) {
                objects.remove(o);
            }
        } else {
            JpaUtil.writeProperty(parent, attribute, null);
        }
    }

    private Object getExistingObject(Class<?> type, EntityManager entityManager, Object id) {

        Object object = getOptionalExistingObject(type, entityManager, id);
        if (object == null) {
            throw AgException.notFound("No object for ID '%s' and entity '%s'", id, type.getSimpleName());
        }

        return object;
    }

    // TODO: use CayenneUtil.findById(..) or CayenneUtil.parentQualifier
    private Object getOptionalExistingObject(Class<?> type, EntityManager entityManager, Object id) {
        return entityManager.find(type, id);
    }
}
