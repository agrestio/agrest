package io.agrest.cayenne.processor.delete.stage;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.processor.ProcessingContext;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.stage.DeleteStartStage;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class CayenneDeleteStartStage extends DeleteStartStage {

    private static final String DELETE_OBJECT_CONTEXT_ATTRIBUTE = "deleteContext";

    /**
     * Returns Cayenne ObjectContext previously stored in the ProcessingContext
     * by this stage.
     */
    public static ObjectContext cayenneContext(ProcessingContext<?> context) {
        return (ObjectContext) context.getProperty(CayenneDeleteStartStage.DELETE_OBJECT_CONTEXT_ATTRIBUTE);
    }

    private final ICayennePersister persister;

    public CayenneDeleteStartStage(@Inject ICayennePersister persister) {
        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        ProcessorOutcome outcome = super.execute(context);
        if (outcome == ProcessorOutcome.CONTINUE) {
            initCayenneContext(context);
        }

        return outcome;
    }

    protected <T> void initCayenneContext(DeleteContext<T> context) {
        context.setProperty(DELETE_OBJECT_CONTEXT_ATTRIBUTE, persister.newContext());
    }
}

