package io.agrest.runtime.processor.select;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;

/**
 * @since 2.7
 */
public class StartStage implements Processor<SelectContext<?>> {

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        return ProcessorOutcome.CONTINUE;
    }
}
