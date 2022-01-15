package io.agrest.cayenne.processor.unrelate;

import io.agrest.UnrelateStage;
import io.agrest.processor.Processor;
import io.agrest.runtime.AgExceptionMappers;
import io.agrest.runtime.processor.unrelate.UnrelateContext;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class CayenneUnrelateProcessorFactoryProvider implements Provider<UnrelateProcessorFactory> {

    private final AgExceptionMappers exceptionMappers;
    private final EnumMap<UnrelateStage, Processor<UnrelateContext<?>>> stages;

    public CayenneUnrelateProcessorFactoryProvider(
            @Inject CayenneUnrelateStartStage startStage,
            @Inject CayenneUnrelateDataStoreStage dataStoreStage,

            @Inject AgExceptionMappers exceptionMappers
    ) {

        this.exceptionMappers = exceptionMappers;

        stages = new EnumMap<>(UnrelateStage.class);
        stages.put(UnrelateStage.START, startStage);
        stages.put(UnrelateStage.UPDATE_DATA_STORE, dataStoreStage);
    }

    @Override
    public UnrelateProcessorFactory get() throws DIRuntimeException {
        return new UnrelateProcessorFactory(stages, exceptionMappers);
    }
}
