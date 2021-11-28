package io.agrest.cayenne.processor.update;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;

/**
 * Handles {@link io.agrest.UpdateStage#COMMIT} stage of the update process.
 *
 * @since 3.6
 */
public class CayenneCommitStage implements Processor<UpdateContext<?>> {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        CayenneUpdateStartStage.cayenneContext(context).commitChanges();
        return ProcessorOutcome.CONTINUE;
    }
}
