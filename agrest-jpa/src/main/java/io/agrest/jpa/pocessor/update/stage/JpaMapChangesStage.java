package io.agrest.jpa.pocessor.update.stage;

import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.stage.UpdateMapChangesStage;

/**
 * A superclass of processors for the {@link io.agrest.UpdateStage#MAP_CHANGES} stage that associates persistent
 * objects with update operations.
 *
 * @since 5.0
 */
public abstract class JpaMapChangesStage extends UpdateMapChangesStage {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        UpdateContext<Object> doContext = (UpdateContext<Object>) context;
        try {
            map(doContext);
        }catch (Throwable e){
            try {
                JpaUpdateStartStage.entityManager(context)
                        .getTransaction()
                        .rollback();
            } finally {
                JpaUpdateStartStage.entityManager(context).close();
            }
            throw e;
        }
        return ProcessorOutcome.CONTINUE;
    }

    protected abstract void map(UpdateContext<Object> context);
}
