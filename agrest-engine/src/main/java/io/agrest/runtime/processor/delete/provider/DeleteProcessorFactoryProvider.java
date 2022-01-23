package io.agrest.runtime.processor.delete.provider;

import io.agrest.DeleteStage;
import io.agrest.processor.Processor;
import io.agrest.runtime.AgExceptionMappers;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.delete.stage.DeleteAuthorizeChangesStage;
import io.agrest.runtime.processor.delete.stage.DeleteInDataStoreStage;
import io.agrest.runtime.processor.delete.stage.DeleteMapChangesStage;
import io.agrest.runtime.processor.delete.stage.DeleteStartStage;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class DeleteProcessorFactoryProvider implements Provider<DeleteProcessorFactory> {

    private final EnumMap<DeleteStage, Processor<DeleteContext<?>>> stages;
    private final AgExceptionMappers exceptionMappers;

    public DeleteProcessorFactoryProvider(
            @Inject DeleteStartStage startStage,
            @Inject DeleteMapChangesStage mapChangesStage,
            @Inject DeleteAuthorizeChangesStage authorizeChangesStage,
            @Inject DeleteInDataStoreStage deleteInDataStoreStage,
            @Inject AgExceptionMappers exceptionMappers) {

        this.exceptionMappers = exceptionMappers;

        stages = new EnumMap<>(DeleteStage.class);
        stages.put(DeleteStage.START, startStage);
        stages.put(DeleteStage.MAP_CHANGES, mapChangesStage);
        stages.put(DeleteStage.AUTHORIZE_CHANGES, authorizeChangesStage);
        stages.put(DeleteStage.DELETE_IN_DATA_STORE, deleteInDataStoreStage);
    }

    @Override
    public DeleteProcessorFactory get() throws DIRuntimeException {
        return new DeleteProcessorFactory(stages, exceptionMappers);
    }
}
