package io.agrest.jpa.pocessor.delete.stage;

import java.util.ArrayList;
import java.util.List;

import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.stage.DeleteInDataStoreStage;
import io.agrest.runtime.processor.update.ChangeOperation;
import jakarta.persistence.EntityManager;

/**
 * @since 5.0
 */
public class JpaDeleteInDataStoreStage extends DeleteInDataStoreStage {

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected void doExecute(DeleteContext<?> context) {

        List<Object> objects = new ArrayList<>(context.getDeleteOperations().size());
        for (ChangeOperation<?> op : context.getDeleteOperations()) {
            objects.add(op.getObject());
        }

        EntityManager entityManager = JpaDeleteStartStage.entityManager(context);

        entityManager.getTransaction().begin();
        objects.forEach(entityManager::detach);
        entityManager.getTransaction().commit();
    }
}
