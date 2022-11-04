package io.agrest.cayenne.processor.update.stage;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.processor.ProcessingContext;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.stage.UpdateStartStage;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class CayenneUpdateStartStage extends UpdateStartStage {

    private static final String UPDATE_OBJECT_CONTEXT_ATTRIBUTE = "updateContext";

    /**
     * Returns Cayenne ObjectContext previously stored in the ProcessingContext
     * by this stage.
     */
    public static ObjectContext cayenneContext(ProcessingContext<?> context) {
        return (ObjectContext) context.getProperty(CayenneUpdateStartStage.UPDATE_OBJECT_CONTEXT_ATTRIBUTE);
    }

    private ICayennePersister persister;

    public CayenneUpdateStartStage(@Inject ICayennePersister persister) {
        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        context.setProperty(UPDATE_OBJECT_CONTEXT_ATTRIBUTE, persister.newContext());
        return ProcessorOutcome.CONTINUE;
    }
}
