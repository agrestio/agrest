package io.agrest.runtime.processor.select.stage;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 2.7
 */
public class SelectStartStage implements Processor<SelectContext<?>> {

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        return ProcessorOutcome.CONTINUE;
    }
}
