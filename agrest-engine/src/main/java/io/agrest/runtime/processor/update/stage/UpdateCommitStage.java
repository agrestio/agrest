package io.agrest.runtime.processor.update.stage;

import io.agrest.UpdateStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;

/**
 * Handles {@link io.agrest.UpdateStage#COMMIT} stage of the update process.
 *
 * @since 5.0
 */
public class UpdateCommitStage implements Processor<UpdateContext<?>> {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        throw new UnsupportedOperationException(
                "No implementation of " + UpdateStage.COMMIT + " stage is available");
    }
}
