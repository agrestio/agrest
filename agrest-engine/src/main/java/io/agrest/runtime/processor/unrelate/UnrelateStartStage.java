package io.agrest.runtime.processor.unrelate;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;

/**
 * @since 5.0
 */
public class UnrelateStartStage implements Processor<UnrelateContext<?>> {

    @Override
    public ProcessorOutcome execute(UnrelateContext<?> context) {
        return ProcessorOutcome.CONTINUE;
    }
}
