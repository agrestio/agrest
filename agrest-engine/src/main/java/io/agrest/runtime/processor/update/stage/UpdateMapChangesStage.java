package io.agrest.runtime.processor.update.stage;

import io.agrest.UpdateStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;

/**
 * Handles {@link UpdateStage#MAP_CHANGES} stage of the update process.
 *
 * @since 5.0
 */
public class UpdateMapChangesStage implements Processor<UpdateContext<?>> {

    private static final UpdateMapChangesStage instance = new UpdateMapChangesStage();

    public static UpdateMapChangesStage getInstance() {
        return instance;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        throw new UnsupportedOperationException(
                "No implementation of " + UpdateStage.MAP_CHANGES + " stage is available");
    }
}
