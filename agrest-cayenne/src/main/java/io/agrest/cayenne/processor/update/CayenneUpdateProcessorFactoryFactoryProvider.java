package io.agrest.cayenne.processor.update;

import io.agrest.UpdateStage;
import io.agrest.processor.Processor;
import io.agrest.runtime.UpdateOperation;
import io.agrest.runtime.processor.update.*;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class CayenneUpdateProcessorFactoryFactoryProvider implements Provider<UpdateProcessorFactoryFactory> {

    private EnumMap<UpdateStage, Processor<UpdateContext<?>>> createStages;
    private EnumMap<UpdateStage, Processor<UpdateContext<?>>> updateStages;
    private EnumMap<UpdateStage, Processor<UpdateContext<?>>> createOrUpdateStages;
    private EnumMap<UpdateStage, Processor<UpdateContext<?>>> idempotentCreateOrUpdateStages;
    private EnumMap<UpdateStage, Processor<UpdateContext<?>>> idempotentFullSyncStages;

    public CayenneUpdateProcessorFactoryFactoryProvider(
            @Inject CayenneUpdateStartStage startStage,
            @Inject ParseRequestStage parseRequestStage,
            @Inject CreateResourceEntityStage createResourceEntityStage,
            @Inject CayenneApplyServerParamsStage applyServerParamsStage,
            @Inject CayenneCreateStage createStage,
            @Inject CayenneUpdateStage updateStage,
            @Inject CayenneCreateOrUpdateStage createOrUpdateStage,
            @Inject CayenneIdempotentCreateOrUpdateStage idempotentCreateOrUpdateStage,
            @Inject CayenneIdempotentFullSyncStage idempotentFullSyncStage,
            @Inject CayenneCommitStage commitStage,
            @Inject CayenneOkResponseStage okResponseStage,
            @Inject CayenneCreatedResponseStage createdResponseStage,
            @Inject CayenneCreatedOrOkResponseStage createdOrOkResponseStage
    ) {

        this.createStages = new EnumMap<>(UpdateStage.class);
        this.createStages.put(UpdateStage.START, startStage);
        this.createStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.createStages.put(UpdateStage.CREATE_ENTITY, createResourceEntityStage);
        this.createStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.createStages.put(UpdateStage.MERGE_CHANGES, createStage);
        this.createStages.put(UpdateStage.COMMIT, commitStage);
        this.createStages.put(UpdateStage.FILL_RESPONSE, createdResponseStage);

        this.updateStages = new EnumMap<>(UpdateStage.class);
        this.updateStages.put(UpdateStage.START, startStage);
        this.updateStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.updateStages.put(UpdateStage.CREATE_ENTITY, createResourceEntityStage);
        this.updateStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.updateStages.put(UpdateStage.MERGE_CHANGES, updateStage);
        this.updateStages.put(UpdateStage.COMMIT, commitStage);
        this.updateStages.put(UpdateStage.FILL_RESPONSE, okResponseStage);

        this.createOrUpdateStages = new EnumMap<>(UpdateStage.class);
        this.createOrUpdateStages.put(UpdateStage.START, startStage);
        this.createOrUpdateStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.createOrUpdateStages.put(UpdateStage.CREATE_ENTITY, createResourceEntityStage);
        this.createOrUpdateStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.createOrUpdateStages.put(UpdateStage.MERGE_CHANGES, createOrUpdateStage);
        this.createOrUpdateStages.put(UpdateStage.COMMIT, commitStage);
        this.createOrUpdateStages.put(UpdateStage.FILL_RESPONSE, createdOrOkResponseStage);

        this.idempotentCreateOrUpdateStages = new EnumMap<>(UpdateStage.class);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.START, startStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.CREATE_ENTITY, createResourceEntityStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.MERGE_CHANGES, idempotentCreateOrUpdateStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.COMMIT, commitStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.FILL_RESPONSE, createdOrOkResponseStage);

        this.idempotentFullSyncStages = new EnumMap<>(UpdateStage.class);
        this.idempotentFullSyncStages.put(UpdateStage.START, startStage);
        this.idempotentFullSyncStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.idempotentFullSyncStages.put(UpdateStage.CREATE_ENTITY, createResourceEntityStage);
        this.idempotentFullSyncStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.idempotentFullSyncStages.put(UpdateStage.MERGE_CHANGES, idempotentFullSyncStage);
        this.idempotentFullSyncStages.put(UpdateStage.COMMIT, commitStage);
        this.idempotentFullSyncStages.put(UpdateStage.FILL_RESPONSE, createdOrOkResponseStage);
    }

    @Override
    public UpdateProcessorFactoryFactory get() throws DIRuntimeException {

        EnumMap<UpdateOperation, UpdateProcessorFactory> factories = new EnumMap<>(UpdateOperation.class);

        factories.put(UpdateOperation.create, new UpdateProcessorFactory(createStages));
        factories.put(UpdateOperation.createOrUpdate, new UpdateProcessorFactory(createOrUpdateStages));
        factories.put(UpdateOperation.idempotentCreateOrUpdate, new UpdateProcessorFactory(idempotentCreateOrUpdateStages));
        factories.put(UpdateOperation.idempotentFullSync, new UpdateProcessorFactory(idempotentFullSyncStages));
        factories.put(UpdateOperation.update, new UpdateProcessorFactory(updateStages));

        return new UpdateProcessorFactoryFactory(factories);
    }
}
