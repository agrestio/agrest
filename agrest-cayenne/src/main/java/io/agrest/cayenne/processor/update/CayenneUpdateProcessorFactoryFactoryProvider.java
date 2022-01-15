package io.agrest.cayenne.processor.update;

import io.agrest.UpdateStage;
import io.agrest.processor.Processor;
import io.agrest.runtime.AgExceptionMappers;
import io.agrest.runtime.UpdateOperation;
import io.agrest.runtime.processor.update.AuthorizeChangesStage;
import io.agrest.runtime.processor.update.CreateResourceEntityStage;
import io.agrest.runtime.processor.update.EncoderInstallStage;
import io.agrest.runtime.processor.update.FilterResultStage;
import io.agrest.runtime.processor.update.ParseRequestStage;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.UpdateProcessorFactory;
import io.agrest.runtime.processor.update.UpdateProcessorFactoryFactory;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class CayenneUpdateProcessorFactoryFactoryProvider implements Provider<UpdateProcessorFactoryFactory> {

    private final AgExceptionMappers exceptionMappers;
    private final EnumMap<UpdateStage, Processor<UpdateContext<?>>> createStages;
    private final EnumMap<UpdateStage, Processor<UpdateContext<?>>> updateStages;
    private final EnumMap<UpdateStage, Processor<UpdateContext<?>>> createOrUpdateStages;
    private final EnumMap<UpdateStage, Processor<UpdateContext<?>>> idempotentCreateOrUpdateStages;
    private final EnumMap<UpdateStage, Processor<UpdateContext<?>>> idempotentFullSyncStages;

    public CayenneUpdateProcessorFactoryFactoryProvider(
            @Inject CayenneUpdateStartStage startStage,
            @Inject ParseRequestStage parseRequestStage,
            @Inject CreateResourceEntityStage createResourceEntityStage,
            @Inject CayenneApplyServerParamsStage applyServerParamsStage,

            @Inject CayenneMapCreateStage mapCreateStage,
            @Inject CayenneMapUpdateStage mapUpdateStage,
            @Inject CayenneMapCreateOrUpdateStage mapCreateOrUpdateStage,
            @Inject CayenneMapIdempotentCreateOrUpdateStage mapIdempotentCreateOrUpdateStage,
            @Inject CayenneMapIdempotentFullSyncStage mapIdempotentFullSyncStage,

            @Inject AuthorizeChangesStage authorizeChangesStage,

            @Inject CayenneMergeChangesStage mergeStage,

            @Inject CayenneCommitStage commitStage,
            @Inject CayenneOkResponseStage okResponseStage,
            @Inject CayenneCreatedResponseStage createdResponseStage,
            @Inject CayenneCreatedOrOkResponseStage createdOrOkResponseStage,

            @Inject FilterResultStage filterResultStage,
            @Inject EncoderInstallStage encoderInstallStage,

            @Inject AgExceptionMappers exceptionMappers
    ) {

        this.exceptionMappers = exceptionMappers;

        this.createStages = new EnumMap<>(UpdateStage.class);
        this.createStages.put(UpdateStage.START, startStage);
        this.createStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.createStages.put(UpdateStage.CREATE_ENTITY, createResourceEntityStage);
        this.createStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.createStages.put(UpdateStage.MAP_CHANGES, mapCreateStage);
        this.createStages.put(UpdateStage.AUTHORIZE_CHANGES, authorizeChangesStage);
        this.createStages.put(UpdateStage.MERGE_CHANGES, mergeStage);
        this.createStages.put(UpdateStage.COMMIT, commitStage);
        this.createStages.put(UpdateStage.FILL_RESPONSE, createdResponseStage);
        this.createStages.put(UpdateStage.FILTER_RESULT, filterResultStage);
        this.createStages.put(UpdateStage.ENCODE, encoderInstallStage);

        this.updateStages = new EnumMap<>(UpdateStage.class);
        this.updateStages.put(UpdateStage.START, startStage);
        this.updateStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.updateStages.put(UpdateStage.CREATE_ENTITY, createResourceEntityStage);
        this.updateStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.updateStages.put(UpdateStage.MAP_CHANGES, mapUpdateStage);
        this.updateStages.put(UpdateStage.AUTHORIZE_CHANGES, authorizeChangesStage);
        this.updateStages.put(UpdateStage.MERGE_CHANGES, mergeStage);
        this.updateStages.put(UpdateStage.COMMIT, commitStage);
        this.updateStages.put(UpdateStage.FILL_RESPONSE, okResponseStage);
        this.updateStages.put(UpdateStage.FILTER_RESULT, filterResultStage);
        this.updateStages.put(UpdateStage.ENCODE, encoderInstallStage);

        this.createOrUpdateStages = new EnumMap<>(UpdateStage.class);
        this.createOrUpdateStages.put(UpdateStage.START, startStage);
        this.createOrUpdateStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.createOrUpdateStages.put(UpdateStage.CREATE_ENTITY, createResourceEntityStage);
        this.createOrUpdateStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.createOrUpdateStages.put(UpdateStage.MAP_CHANGES, mapCreateOrUpdateStage);
        this.createOrUpdateStages.put(UpdateStage.AUTHORIZE_CHANGES, authorizeChangesStage);
        this.createOrUpdateStages.put(UpdateStage.MERGE_CHANGES, mergeStage);
        this.createOrUpdateStages.put(UpdateStage.COMMIT, commitStage);
        this.createOrUpdateStages.put(UpdateStage.FILL_RESPONSE, createdOrOkResponseStage);
        this.createOrUpdateStages.put(UpdateStage.FILTER_RESULT, filterResultStage);
        this.createOrUpdateStages.put(UpdateStage.ENCODE, encoderInstallStage);

        this.idempotentCreateOrUpdateStages = new EnumMap<>(UpdateStage.class);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.START, startStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.CREATE_ENTITY, createResourceEntityStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.MAP_CHANGES, mapIdempotentCreateOrUpdateStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.AUTHORIZE_CHANGES, authorizeChangesStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.MERGE_CHANGES, mergeStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.COMMIT, commitStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.FILL_RESPONSE, createdOrOkResponseStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.FILTER_RESULT, filterResultStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.ENCODE, encoderInstallStage);

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
    public UpdateProcessorFactoryFactory get() throws DIRuntimeException {

        EnumMap<UpdateOperation, UpdateProcessorFactory> factories = new EnumMap<>(UpdateOperation.class);

        factories.put(UpdateOperation.create, new UpdateProcessorFactory(createStages, exceptionMappers));
        factories.put(UpdateOperation.createOrUpdate, new UpdateProcessorFactory(createOrUpdateStages, exceptionMappers));
        factories.put(UpdateOperation.idempotentCreateOrUpdate, new UpdateProcessorFactory(idempotentCreateOrUpdateStages, exceptionMappers));
        factories.put(UpdateOperation.idempotentFullSync, new UpdateProcessorFactory(idempotentFullSyncStages, exceptionMappers));
        factories.put(UpdateOperation.update, new UpdateProcessorFactory(updateStages, exceptionMappers));

        return new UpdateProcessorFactoryFactory(factories);
    }
}
