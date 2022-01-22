package io.agrest.cayenne.processor.update.provider;

import io.agrest.UpdateStage;
import io.agrest.cayenne.processor.update.CayenneApplyServerParamsStage;
import io.agrest.cayenne.processor.update.CayenneCommitStage;
import io.agrest.cayenne.processor.update.CayenneCreatedOrOkResponseStage;
import io.agrest.cayenne.processor.update.CayenneMapIdempotentFullSyncStage;
import io.agrest.cayenne.processor.update.CayenneMergeChangesStage;
import io.agrest.cayenne.processor.update.CayenneUpdateStartStage;
import io.agrest.processor.Processor;
import io.agrest.runtime.AgExceptionMappers;
import io.agrest.runtime.processor.update.AuthorizeChangesStage;
import io.agrest.runtime.processor.update.CreateResourceEntityStage;
import io.agrest.runtime.processor.update.EncoderInstallStage;
import io.agrest.runtime.processor.update.FilterResultStage;
import io.agrest.runtime.processor.update.IdempotentFullSyncProcessorFactory;
import io.agrest.runtime.processor.update.ParseRequestStage;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 5.0
 */
public class CayenneIdempotentFullSyncProcessorFactoryProvider implements Provider<IdempotentFullSyncProcessorFactory> {

    private final AgExceptionMappers exceptionMappers;
    private final EnumMap<UpdateStage, Processor<UpdateContext<?>>> idempotentFullSyncStages;

    public CayenneIdempotentFullSyncProcessorFactoryProvider(
            @Inject CayenneUpdateStartStage startStage,
            @Inject ParseRequestStage parseRequestStage,
            @Inject CreateResourceEntityStage createResourceEntityStage,
            @Inject CayenneApplyServerParamsStage applyServerParamsStage,
            @Inject CayenneMapIdempotentFullSyncStage mapIdempotentFullSyncStage,
            @Inject AuthorizeChangesStage authorizeChangesStage,
            @Inject CayenneMergeChangesStage mergeStage,
            @Inject CayenneCommitStage commitStage,
            @Inject CayenneCreatedOrOkResponseStage createdOrOkResponseStage,
            @Inject FilterResultStage filterResultStage,
            @Inject EncoderInstallStage encoderInstallStage,
            @Inject AgExceptionMappers exceptionMappers
    ) {

        this.exceptionMappers = exceptionMappers;

        this.idempotentFullSyncStages = new EnumMap<>(UpdateStage.class);
        this.idempotentFullSyncStages.put(UpdateStage.START, startStage);
        this.idempotentFullSyncStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.idempotentFullSyncStages.put(UpdateStage.CREATE_ENTITY, createResourceEntityStage);
        this.idempotentFullSyncStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.idempotentFullSyncStages.put(UpdateStage.MAP_CHANGES, mapIdempotentFullSyncStage);
        this.idempotentFullSyncStages.put(UpdateStage.AUTHORIZE_CHANGES, authorizeChangesStage);
        this.idempotentFullSyncStages.put(UpdateStage.MERGE_CHANGES, mergeStage);
        this.idempotentFullSyncStages.put(UpdateStage.COMMIT, commitStage);
        this.idempotentFullSyncStages.put(UpdateStage.FILL_RESPONSE, createdOrOkResponseStage);
        this.idempotentFullSyncStages.put(UpdateStage.FILTER_RESULT, filterResultStage);
        this.idempotentFullSyncStages.put(UpdateStage.ENCODE, encoderInstallStage);
    }

    @Override
    public IdempotentFullSyncProcessorFactory get() throws DIRuntimeException {
       return new IdempotentFullSyncProcessorFactory(idempotentFullSyncStages, exceptionMappers);
    }
}
