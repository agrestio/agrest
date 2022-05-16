package io.agrest.jpa.pocessor.delete.stage;

import io.agrest.jpa.persister.AgJpaPersister;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.JpaProcessor;
import io.agrest.meta.AgDataMap;
import io.agrest.processor.ProcessingContext;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.stage.DeleteStartStage;
import jakarta.persistence.EntityManager;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaDeleteStartStage extends DeleteStartStage {


    /**
     * Returns Cayenne ObjectContext previously stored in the ProcessingContext
     * by this stage.
     */
    public static EntityManager entityManager(ProcessingContext<?> context) {
        return (EntityManager) context.getAttribute(AgJpaPersister.ENTITY_MANAGER_KEY);
    }

    private final IAgJpaPersister persister;

    public JpaDeleteStartStage(@Inject AgDataMap dataMap, @Inject IAgJpaPersister persister) {
        super(dataMap);
        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        ProcessorOutcome outcome = super.execute(context);
        if (outcome == ProcessorOutcome.CONTINUE) {
            initEntityManager(context);
        }

        return outcome;
    }

    protected <T> void initEntityManager(DeleteContext<T> context) {
        context.setAttribute(AgJpaPersister.ENTITY_MANAGER_KEY, persister.entityManager());
    }
}

