package io.agrest.runtime.processor.unrelate;

import io.agrest.UnrelateStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;

/**
 * @since 5.0
 */
public class UnrelateUpdateDateStoreStage implements Processor<UnrelateContext<?>> {

    @Override
    public ProcessorOutcome execute(UnrelateContext<?> context) {
        throw new UnsupportedOperationException(
                "No implementation of " + UnrelateStage.UPDATE_DATA_STORE + " stage is available");
    }
}
