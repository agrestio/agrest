package io.agrest.cayenne.processor.update.stage;

import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.stage.UpdateCommitStage;
import io.agrest.runtime.processor.update.UpdateContext;

/**
 * Handles {@link io.agrest.UpdateStage#COMMIT} stage of the update process.
 *
 * @since 5.0
 */
public class CayenneUpdateCommitStage extends UpdateCommitStage {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        CayenneUpdateStartStage.cayenneContext(context).commitChanges();
        return ProcessorOutcome.CONTINUE;
    }
}
