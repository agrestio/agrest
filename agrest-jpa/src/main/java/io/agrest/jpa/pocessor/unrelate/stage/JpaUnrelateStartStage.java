package io.agrest.jpa.pocessor.unrelate.stage;

import io.agrest.jpa.persister.AgJpaPersister;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.processor.ProcessingContext;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.unrelate.UnrelateContext;
import io.agrest.runtime.processor.unrelate.stage.UnrelateStartStage;
import jakarta.persistence.EntityManager;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaUnrelateStartStage extends UnrelateStartStage {

    /**
     * Returns Cayenne ObjectContext previously stored in the ProcessingContext
     * by this stage.
     */
    public static EntityManager entityManager(ProcessingContext<?> context) {
        return (EntityManager) context.getAttribute(AgJpaPersister.ENTITY_MANAGER_KEY);
    }

    private final IAgJpaPersister persister;

    public JpaUnrelateStartStage(@Inject IAgJpaPersister persister) {
        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(UnrelateContext<?> context) {
        context.setAttribute(AgJpaPersister.ENTITY_MANAGER_KEY, persister.entityManager());
        return ProcessorOutcome.CONTINUE;
    }
}

