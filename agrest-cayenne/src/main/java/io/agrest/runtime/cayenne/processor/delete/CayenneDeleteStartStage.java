package io.agrest.runtime.cayenne.processor.delete;

import io.agrest.processor.ProcessingContext;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.IAgPersister;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.delete.DeleteContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class CayenneDeleteStartStage implements Processor<DeleteContext<?>> {

    private static final String DELETE_OBJECT_CONTEXT_ATTRIBITE = "deleteContext";

    /**
     * Returns Cayenne ObjectContext previously stored in the ProcessingContext
     * by this stage.
     */
    public static ObjectContext cayenneContext(ProcessingContext<?> context) {
        return (ObjectContext) context.getAttribute(CayenneDeleteStartStage.DELETE_OBJECT_CONTEXT_ATTRIBITE);
    }

    private IAgPersister persister;

    public CayenneDeleteStartStage(@Inject IAgPersister persister) {
        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        context.setAttribute(DELETE_OBJECT_CONTEXT_ATTRIBITE, persister.newContext());
        return ProcessorOutcome.CONTINUE;
    }
}

