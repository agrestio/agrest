package io.agrest.cayenne.processor.unrelate;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.processor.ProcessingContext;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.unrelate.UnrelateContext;
import io.agrest.runtime.processor.unrelate.UnrelateStartStage;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class CayenneUnrelateStartStage extends UnrelateStartStage {

    private static final String UNRELATE_OBJECT_CONTEXT_ATTRIBITE = "unrelateContext";

    /**
     * Returns Cayenne ObjectContext previously stored in the ProcessingContext
     * by this stage.
     */
    public static ObjectContext cayenneContext(ProcessingContext<?> context) {
        return (ObjectContext) context.getAttribute(CayenneUnrelateStartStage.UNRELATE_OBJECT_CONTEXT_ATTRIBITE);
    }

    private final ICayennePersister persister;

    public CayenneUnrelateStartStage(@Inject ICayennePersister persister) {
        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(UnrelateContext<?> context) {
        context.setAttribute(UNRELATE_OBJECT_CONTEXT_ATTRIBITE, persister.newContext());
        return ProcessorOutcome.CONTINUE;
    }
}

