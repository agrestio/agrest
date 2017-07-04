package com.nhl.link.rest.runtime.cayenne.processor.unrelate;

import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateContext;
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

    private ICayennePersister persister;

    public CayenneUnrelateStartStage(@Inject ICayennePersister persister) {
        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(UnrelateContext<?> context) {
        context.setAttribute(UNRELATE_OBJECT_CONTEXT_ATTRIBITE, persister.newContext());
        return ProcessorOutcome.CONTINUE;
    }
}

