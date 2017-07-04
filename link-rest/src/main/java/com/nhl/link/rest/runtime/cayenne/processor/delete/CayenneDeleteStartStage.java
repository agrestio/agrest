package com.nhl.link.rest.runtime.cayenne.processor.delete;

import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;
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

    private ICayennePersister persister;

    public CayenneDeleteStartStage(@Inject ICayennePersister persister) {
        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        context.setAttribute(DELETE_OBJECT_CONTEXT_ATTRIBITE, persister.newContext());
        return ProcessorOutcome.CONTINUE;
    }
}

