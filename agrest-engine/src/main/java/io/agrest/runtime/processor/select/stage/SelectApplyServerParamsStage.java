package io.agrest.runtime.processor.select.stage;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.constraints.SelectConstraints;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class SelectApplyServerParamsStage implements Processor<SelectContext<?>> {

    private final SelectConstraints constraints;

    public SelectApplyServerParamsStage(@Inject SelectConstraints constraints) {
        this.constraints = constraints;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        constraints.apply(context);
    }
}