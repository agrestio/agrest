package io.agrest.runtime.processor.update.provider;

import io.agrest.UpdateStage;
import io.agrest.processor.Processor;
import io.agrest.runtime.AgExceptionMappers;
import io.agrest.runtime.processor.update.CreateProcessorFactory;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.UpdateFlavorDIKeys;
import io.agrest.runtime.processor.update.stage.UpdateApplyServerParamsStage;
import io.agrest.runtime.processor.update.stage.UpdateAuthorizeChangesStage;
import io.agrest.runtime.processor.update.stage.UpdateCommitStage;
import io.agrest.runtime.processor.update.stage.UpdateCreateResourceEntityStage;
import io.agrest.runtime.processor.update.stage.UpdateEncoderInstallStage;
import io.agrest.runtime.processor.update.stage.UpdateFillResponseStage;
import io.agrest.runtime.processor.update.stage.UpdateFilterResultStage;
import io.agrest.runtime.processor.update.stage.UpdateMapChangesStage;
import io.agrest.runtime.processor.update.stage.UpdateMergeChangesStage;
import io.agrest.runtime.processor.update.stage.UpdateParseRequestStage;
import io.agrest.runtime.processor.update.stage.UpdateStartStage;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 5.0
 */
public class CreateProcessorFactoryProvider implements Provider<CreateProcessorFactory> {

    private final AgExceptionMappers exceptionMappers;
    private final EnumMap<UpdateStage, Processor<UpdateContext<?>>> stages;

    public CreateProcessorFactoryProvider(
            @Inject UpdateStartStage startStage,
            @Inject UpdateParseRequestStage parseRequestStage,
            @Inject UpdateCreateResourceEntityStage createResourceEntityStage,
            @Inject UpdateApplyServerParamsStage applyServerParamsStage,
            @Inject(UpdateFlavorDIKeys.CREATE) UpdateMapChangesStage mapChangesStage,
            @Inject UpdateAuthorizeChangesStage authorizeChangesStage,
            @Inject UpdateMergeChangesStage mergeStage,
            @Inject UpdateCommitStage commitStage,
            @Inject(UpdateFlavorDIKeys.CREATE) UpdateFillResponseStage fillResponseStage,
            @Inject UpdateFilterResultStage filterResultStage,
            @Inject UpdateEncoderInstallStage encoderInstallStage,

            @Inject AgExceptionMappers exceptionMappers
    ) {

        this.exceptionMappers = exceptionMappers;

        this.stages = new EnumMap<>(UpdateStage.class);
        this.stages.put(UpdateStage.START, startStage);
        this.stages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.stages.put(UpdateStage.CREATE_ENTITY, createResourceEntityStage);
        this.stages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.stages.put(UpdateStage.MAP_CHANGES, mapChangesStage);
        this.stages.put(UpdateStage.AUTHORIZE_CHANGES, authorizeChangesStage);
        this.stages.put(UpdateStage.MERGE_CHANGES, mergeStage);
        this.stages.put(UpdateStage.COMMIT, commitStage);
        this.stages.put(UpdateStage.FILL_RESPONSE, fillResponseStage);
        this.stages.put(UpdateStage.FILTER_RESULT, filterResultStage);
        this.stages.put(UpdateStage.ENCODE, encoderInstallStage);
    }

    @Override
    public CreateProcessorFactory get() throws DIRuntimeException {
        return new CreateProcessorFactory(stages, exceptionMappers);
    }
}
