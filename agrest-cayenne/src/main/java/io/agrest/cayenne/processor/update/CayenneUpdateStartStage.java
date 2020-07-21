package io.agrest.cayenne.processor.update;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.processor.ProcessingContext;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class CayenneUpdateStartStage implements Processor<UpdateContext<?>> {

    private static final String UPDATE_OBJECT_CONTEXT_ATTRUBITE = "updateContext";

    /**
     * Returns Cayenne ObjectContext previously stored in the ProcessingContext
     * by this stage.
     */
    public static ObjectContext cayenneContext(ProcessingContext<?> context) {
        return (ObjectContext) context.getAttribute(CayenneUpdateStartStage.UPDATE_OBJECT_CONTEXT_ATTRUBITE);
    }

    private ICayennePersister persister;

    public CayenneUpdateStartStage(@Inject ICayennePersister persister) {
        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        context.setAttribute(UPDATE_OBJECT_CONTEXT_ATTRUBITE, persister.newContext());
        return ProcessorOutcome.CONTINUE;
    }
}
