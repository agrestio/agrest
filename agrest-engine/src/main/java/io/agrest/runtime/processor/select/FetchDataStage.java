package io.agrest.runtime.processor.select;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;

/**
 * @since 3.4
 */
public class FetchDataStage implements Processor<SelectContext<?>> {

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        context.getEntity().getResolver().fetchData(context);
    }
}
