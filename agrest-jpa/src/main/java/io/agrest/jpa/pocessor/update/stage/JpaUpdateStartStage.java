package io.agrest.jpa.pocessor.update.stage;

import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.processor.ProcessingContext;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.stage.UpdateStartStage;
import jakarta.persistence.EntityManager;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaUpdateStartStage extends UpdateStartStage {

    private static final String UPDATE_ENTITY_MANAGER_ATTRIBUTE = "updateEntityManager";

    /**
     * Returns Cayenne ObjectContext previously stored in the ProcessingContext
     * by this stage.
     */
    public static EntityManager entityManager(ProcessingContext<?> context) {
        return (EntityManager) context.getAttribute(UPDATE_ENTITY_MANAGER_ATTRIBUTE);
    }

    private IAgJpaPersister persister;

    public JpaUpdateStartStage(@Inject IAgJpaPersister persister) {
        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        EntityManager entityManager = persister.entityManager();
        entityManager.getTransaction().begin();
        context.setAttribute(UPDATE_ENTITY_MANAGER_ATTRIBUTE, entityManager);
        return ProcessorOutcome.CONTINUE;
    }
}
