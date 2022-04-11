package io.agrest.jpa.pocessor.update.stage;

import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.stage.UpdateCommitStage;
import jakarta.persistence.EntityManager;

/**
 * Handles {@link io.agrest.UpdateStage#COMMIT} stage of the update process.
 *
 * @since 5.0
 */
public class JpaUpdateCommitStage extends UpdateCommitStage {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        EntityManager entityManager = JpaUpdateStartStage.entityManager(context);
        entityManager.getTransaction().commit();
        return ProcessorOutcome.CONTINUE;
    }
}
