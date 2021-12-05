package io.agrest.cayenne.processor.delete;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.ProcessingContext;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class CayenneDeleteStartStage implements Processor<DeleteContext<?>> {

    private static final String DELETE_OBJECT_CONTEXT_ATTRIBUTE = "deleteContext";

    /**
     * Returns Cayenne ObjectContext previously stored in the ProcessingContext
     * by this stage.
     */
    public static ObjectContext cayenneContext(ProcessingContext<?> context) {
        return (ObjectContext) context.getAttribute(CayenneDeleteStartStage.DELETE_OBJECT_CONTEXT_ATTRIBUTE);
    }

    private final ICayennePersister persister;
    private final AgDataMap dataMap;

    public CayenneDeleteStartStage(@Inject ICayennePersister persister, @Inject AgDataMap dataMap) {
        this.persister = persister;
        this.dataMap = dataMap;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        initAgEntity(context);
        initCayenneContext(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void initAgEntity(DeleteContext<T> context) {
        AgEntityOverlay<T> overlay = context.getEntityOverlay();
        AgEntity<T> entity = dataMap.getEntity(context.getType());
        context.setAgEntity(
                overlay != null ? overlay.resolve(dataMap, entity) : entity
        );
    }

    protected <T> void initCayenneContext(DeleteContext<T> context) {
        context.setAttribute(DELETE_OBJECT_CONTEXT_ATTRIBUTE, persister.newContext());
    }
}

