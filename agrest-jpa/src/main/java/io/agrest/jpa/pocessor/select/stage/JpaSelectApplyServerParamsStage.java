package io.agrest.jpa.pocessor.select.stage;

import io.agrest.NestedResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.JpaProcessor;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.processor.select.stage.SelectApplyServerParamsStage;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaSelectApplyServerParamsStage extends SelectApplyServerParamsStage {

    private final IAgJpaPersister persister;

    public JpaSelectApplyServerParamsStage(@Inject IConstraintsHandler constraintsHandler,
                                           @Inject IAgJpaPersister persister) {
        super(constraintsHandler);
        this.persister = persister;
    }

    @Override
    protected <T> void doExecute(SelectContext<T> context) {
        super.doExecute(context);
        tagRootEntity(context.getEntity());
    }

    private void tagRootEntity(RootResourceEntity<?> entity) {
        if(persister.metamodel().entity(entity.getType()) != null) {
            JpaProcessor.getOrCreateRootEntity(entity);
        }

        if (entity.getMapBy() != null) {
            for (NestedResourceEntity<?> child : entity.getMapBy().getChildren().values()) {
                tagNestedEntity(child);
            }
        }

        for (NestedResourceEntity<?> child : entity.getChildren().values()) {
            tagNestedEntity(child);
        }
    }

    private void tagNestedEntity(NestedResourceEntity<?> entity) {
        if(persister.metamodel().entity(entity.getType()) != null) {
            JpaProcessor.getOrCreateNestedEntity(entity);
        }

        if (entity.getMapBy() != null) {
            for (NestedResourceEntity<?> child : entity.getMapBy().getChildren().values()) {
                tagNestedEntity(child);
            }
        }

        for (NestedResourceEntity<?> child : entity.getChildren().values()) {
            tagNestedEntity(child);
        }
    }
}
