package io.agrest.runtime.cayenne.processor.unrelate;

import io.agrest.processor.ProcessingContext;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.IAgPersister;
import io.agrest.runtime.processor.unrelate.UnrelateContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class CayenneUnrelateStartStage implements Processor<UnrelateContext<?>> {

    private static final String UNRELATE_OBJECT_CONTEXT_ATTRIBITE = "unrelateContext";

    /**
     * Returns Cayenne ObjectContext previously stored in the ProcessingContext
     * by this stage.
     */
    public static ObjectContext cayenneContext(ProcessingContext<?> context) {
        return (ObjectContext) context.getAttribute(CayenneUnrelateStartStage.UNRELATE_OBJECT_CONTEXT_ATTRIBITE);
    }

    private IAgPersister persister;

    public CayenneUnrelateStartStage(@Inject IAgPersister persister) {
        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(UnrelateContext<?> context) {
        context.setAttribute(UNRELATE_OBJECT_CONTEXT_ATTRIBITE, persister.newContext());
        return ProcessorOutcome.CONTINUE;
    }
}

