package io.agrest.cayenne.processor.update.provider;

import io.agrest.UpdateStage;
import io.agrest.cayenne.processor.update.CayenneApplyServerParamsStage;
import io.agrest.cayenne.processor.update.CayenneCommitStage;
import io.agrest.cayenne.processor.update.CayenneMapUpdateStage;
import io.agrest.cayenne.processor.update.CayenneMergeChangesStage;
import io.agrest.cayenne.processor.update.CayenneOkResponseStage;
import io.agrest.cayenne.processor.update.CayenneUpdateStartStage;
import io.agrest.processor.Processor;
import io.agrest.runtime.AgExceptionMappers;
import io.agrest.runtime.processor.update.AuthorizeChangesStage;
import io.agrest.runtime.processor.update.CreateResourceEntityStage;
import io.agrest.runtime.processor.update.EncoderInstallStage;
import io.agrest.runtime.processor.update.FilterResultStage;
import io.agrest.runtime.processor.update.ParseRequestStage;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.UpdateProcessorFactory;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 5.0
 */
public class CayenneUpdateProcessorFactoryProvider implements Provider<UpdateProcessorFactory> {

    private final AgExceptionMappers exceptionMappers;
    private final EnumMap<UpdateStage, Processor<UpdateContext<?>>> stages;

    public CayenneUpdateProcessorFactoryProvider(
            @Inject CayenneUpdateStartStage startStage,
            @Inject ParseRequestStage parseRequestStage,
            @Inject CreateResourceEntityStage createResourceEntityStage,
            @Inject CayenneApplyServerParamsStage applyServerParamsStage,
            @Inject CayenneMapUpdateStage mapUpdateStage,
            @Inject AuthorizeChangesStage authorizeChangesStage,
            @Inject CayenneMergeChangesStage mergeStage,
            @Inject CayenneCommitStage commitStage,
            @Inject CayenneOkResponseStage okResponseStage,
            @Inject FilterResultStage filterResultStage,
            @Inject EncoderInstallStage encoderInstallStage,
            @Inject AgExceptionMappers exceptionMappers
    ) {

        this.exceptionMappers = exceptionMappers;

        this.stages = new EnumMap<>(UpdateStage.class);
        this.stages.put(UpdateStage.START, startStage);
        this.stages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.stages.put(UpdateStage.CREATE_ENTITY, createResourceEntityStage);
        this.stages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.stages.put(UpdateStage.MAP_CHANGES, mapUpdateStage);
        this.stages.put(UpdateStage.AUTHORIZE_CHANGES, authorizeChangesStage);
        this.stages.put(UpdateStage.MERGE_CHANGES, mergeStage);
        this.stages.put(UpdateStage.COMMIT, commitStage);
        this.stages.put(UpdateStage.FILL_RESPONSE, okResponseStage);
        this.stages.put(UpdateStage.FILTER_RESULT, filterResultStage);
        this.stages.put(UpdateStage.ENCODE, encoderInstallStage);
    }

    @Override
    public UpdateProcessorFactory get() throws DIRuntimeException {
        return new UpdateProcessorFactory(stages, exceptionMappers);
    }
}
