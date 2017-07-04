package com.nhl.link.rest.runtime.cayenne.processor.delete;

import com.nhl.link.rest.DeleteStage;
import com.nhl.link.rest.processor2.Processor;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;
import com.nhl.link.rest.runtime.processor.delete.DeleteProcessorFactory;
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
