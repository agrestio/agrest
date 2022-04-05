package io.agrest.jpa.pocessor.select.stage;

import io.agrest.RootResourceEntity;
import io.agrest.jpa.pocessor.JpaProcessor;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.processor.select.stage.SelectApplyServerParamsStage;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaSelectApplyServerParamsStage extends SelectApplyServerParamsStage {

    public JpaSelectApplyServerParamsStage(@Inject IConstraintsHandler constraintsHandler) {
        super(constraintsHandler);
    }

    @Override
    protected <T> void doExecute(SelectContext<T> context) {
        super.doExecute(context);
        tagRootEntity(context.getEntity());
    }

    private void tagRootEntity(RootResourceEntity<?> entity) {
        // TODO: check that this is in fact JPA-managed entity
        JpaProcessor.getOrCreateRootEntity(entity);
    }
}
