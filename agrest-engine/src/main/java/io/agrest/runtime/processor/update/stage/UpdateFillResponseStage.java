package io.agrest.runtime.processor.update.stage;

import io.agrest.UpdateStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;

/**
 * A processor invoked for {@link UpdateStage#MERGE_CHANGES} stage.
 *
 * @since 5.0
 */
public class UpdateFillResponseStage implements Processor<UpdateContext<?>> {

    private static final UpdateFillResponseStage instance = new UpdateFillResponseStage();

    public static UpdateFillResponseStage getInstance() {
        return instance;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        throw new UnsupportedOperationException(
                "No implementation of " + UpdateStage.FILL_RESPONSE + " stage is available");
    }
}
