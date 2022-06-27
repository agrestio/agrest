package io.agrest.jpa.pocessor.update.stage;

import java.util.Collections;
import io.agrest.EntityUpdate;
import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.JpaProcessor;
import io.agrest.meta.AgEntity;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.stage.UpdateApplyServerParamsStage;
import jakarta.persistence.metamodel.Metamodel;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaUpdateApplyServerParamsStage extends UpdateApplyServerParamsStage {

    private final IConstraintsHandler constraintsHandler;
    private final Metamodel metamodel;

    public JpaUpdateApplyServerParamsStage(
            @Inject IConstraintsHandler constraintsHandler,
            @Inject IAgJpaPersister persister) {

        this.metamodel = persister.metamodel();
        this.constraintsHandler = constraintsHandler;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {

        ResourceEntity<T> entity = context.getEntity();

        // this creates a new EntityUpdate if there's no request payload, but the context ID is present,
        // so execute it unconditionally
        fillIdsFromExplicitContextId(context);

        // TODO: need to check if we can (need to) get related IDs
//        if (!context.getUpdates().isEmpty()) {
//            fillIdsFromRelatedIds(context);
//            fillIdsFromMeaningfulPk(context);
//            fillIdsFromParentId(context);
//        }

        constraintsHandler.constrainUpdate(context);

        // apply read constraints
        // TODO: should we only care about response constraints after the commit?
        constraintsHandler.constrainResponse(entity, null);

        tagRootEntity(context.getEntity());
    }

    private void tagRootEntity(RootResourceEntity<?> entity) {
        if(metamodel.entity(entity.getType()) != null) {
            JpaProcessor.getOrCreateRootEntity(entity);
        }

        if (entity.getMapBy() != null) {
            for (RelatedResourceEntity<?> child : entity.getMapBy().getChildren().values()) {
                tagNestedEntity(child);
            }
        }

        for (RelatedResourceEntity<?> child : entity.getChildren().values()) {
            tagNestedEntity(child);
        }
    }

    private void tagNestedEntity(RelatedResourceEntity<?> entity) {
        if(metamodel.entity(entity.getType()) != null) {
            JpaProcessor.getOrCreateNestedEntity(entity);
        }

        if (entity.getMapBy() != null) {
            for (RelatedResourceEntity<?> child : entity.getMapBy().getChildren().values()) {
                tagNestedEntity(child);
            }
        }

        for (RelatedResourceEntity<?> child : entity.getChildren().values()) {
            tagNestedEntity(child);
        }
    }

    private <T> void fillIdsFromExplicitContextId(UpdateContext<T> context) {

        if (context.isById()) {

            // id was specified explicitly ... this means a few things:
            // * we expect zero or one object in the body
            // * if zero, create an empty update that will be attached to the ID.
            // * if more than one - throw...

            if (context.getUpdates().isEmpty()) {
                context.setUpdates(Collections.singleton(new EntityUpdate<>(context.getEntity().getAgEntity())));
            }

            AgEntity<T> entity = context.getEntity().getAgEntity();
            EntityUpdate<T> u = context.getFirst();
            u.getOrCreateId().putAll(context.getId().asMap(entity));
            u.setExplicitId();
        }
    }

}
