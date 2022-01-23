package io.agrest.runtime.processor.unrelate;

import io.agrest.UnrelateStage;
import io.agrest.processor.Processor;
import io.agrest.runtime.AgExceptionMappers;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class UnrelateProcessorFactoryProvider implements Provider<UnrelateProcessorFactory> {

    private final AgExceptionMappers exceptionMappers;
    private final EnumMap<UnrelateStage, Processor<UnrelateContext<?>>> stages;

    public UnrelateProcessorFactoryProvider(
            @Inject UnrelateStartStage startStage,
            @Inject UnrelateUpdateDateStoreStage dataStoreStage,

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
