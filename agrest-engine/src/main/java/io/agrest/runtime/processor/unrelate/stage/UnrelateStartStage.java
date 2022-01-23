package io.agrest.runtime.processor.unrelate.stage;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.unrelate.UnrelateContext;

/**
 * @since 5.0
 */
public class UnrelateStartStage implements Processor<UnrelateContext<?>> {

    @Override
    public ProcessorOutcome execute(UnrelateContext<?> context) {
        return ProcessorOutcome.CONTINUE;
    }
}
