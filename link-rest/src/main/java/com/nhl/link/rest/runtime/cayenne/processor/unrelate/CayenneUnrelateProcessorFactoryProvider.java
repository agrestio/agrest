package com.nhl.link.rest.runtime.cayenne.processor.unrelate;

import com.nhl.link.rest.UnrelateStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateContext;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateProcessorFactory;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class CayenneUnrelateProcessorFactoryProvider implements Provider<UnrelateProcessorFactory> {

    private EnumMap<UnrelateStage, Processor<UnrelateContext<?>>> stages;

    public CayenneUnrelateProcessorFactoryProvider(
            @Inject CayenneUnrelateStartStage startStage,
            @Inject CayenneUnrelateDataStoreStage dataStoreStage
    ) {
        stages = new EnumMap<>(UnrelateStage.class);
        stages.put(UnrelateStage.START, startStage);
        stages.put(UnrelateStage.UPDATE_DATA_STORE, dataStoreStage);
    }

    @Override
    public UnrelateProcessorFactory get() throws DIRuntimeException {
        return new UnrelateProcessorFactory(stages);
    }
}
