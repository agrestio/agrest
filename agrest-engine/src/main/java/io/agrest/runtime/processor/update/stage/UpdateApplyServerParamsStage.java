package io.agrest.runtime.processor.update.stage;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;

/**
 * @since 2.7
 */
public class UpdateApplyServerParamsStage implements Processor<UpdateContext<?>> {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        return ProcessorOutcome.CONTINUE;
    }
}
