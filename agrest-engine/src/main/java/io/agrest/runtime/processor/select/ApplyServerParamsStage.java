package io.agrest.runtime.processor.select;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.constraints.IConstraintsHandler;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class ApplyServerParamsStage implements Processor<SelectContext<?>> {

    private final IConstraintsHandler constraintsHandler;

    public ApplyServerParamsStage(@Inject IConstraintsHandler constraintsHandler) {
        this.constraintsHandler = constraintsHandler;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        constraintsHandler.constrainResponse(context.getEntity(), context.getSizeConstraints(), context.getConstraint());
    }
}