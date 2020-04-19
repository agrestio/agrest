package io.agrest.cayenne.processor.delete;

import io.agrest.DeleteStage;
import io.agrest.processor.Processor;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class CayenneDeleteProcessorFactoryProvider implements Provider<DeleteProcessorFactory> {

    private EnumMap<DeleteStage, Processor<DeleteContext<?>>> stages;

    public CayenneDeleteProcessorFactoryProvider(
            @Inject CayenneDeleteStartStage startStage,
            @Inject CayenneDeleteStage deleteStage
    ) {

        stages = new EnumMap<>(DeleteStage.class);
        stages.put(DeleteStage.START, startStage);
        stages.put(DeleteStage.DELETE_IN_DATA_STORE, deleteStage);
    }

    @Override
    public DeleteProcessorFactory get() {
        return new DeleteProcessorFactory(stages);
    }
}
