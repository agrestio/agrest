package com.nhl.link.rest.runtime.cayenne.processor;

import com.nhl.link.rest.UpdateStage;
import com.nhl.link.rest.processor2.Processor;
import com.nhl.link.rest.runtime.UpdateOperation;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneCreateOrUpdateStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneCreateStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneCreatedResponseStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneIdempotentCreateOrUpdateStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneIdempotentFullSyncStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneOkResponseStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneStartStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneUpdateStage;
import com.nhl.link.rest.runtime.processor.update.ApplyServerParamsStage;
import com.nhl.link.rest.runtime.processor.update.ParseRequestStage;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;
import com.nhl.link.rest.runtime.processor.update.UpdateProcessorFactory;
import com.nhl.link.rest.runtime.processor.update.UpdateProcessorFactoryFactory;
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
            @Inject CayenneStartStage startStage,
            @Inject ParseRequestStage parseRequestStage,
            @Inject ApplyServerParamsStage applyServerParamsStage,
            @Inject CayenneCreateStage createStage,
            @Inject CayenneUpdateStage updateStage,
            @Inject CayenneCreateOrUpdateStage createOrUpdateStage,
            @Inject CayenneIdempotentCreateOrUpdateStage idempotentCreateOrUpdateStage,
            @Inject CayenneIdempotentFullSyncStage idempotentFullSyncStage,
            @Inject CayenneOkResponseStage okResponseStage,
            @Inject CayenneCreatedResponseStage createdResponseStage
    ) {

        this.createStages = new EnumMap<>(UpdateStage.class);
        this.createStages.put(UpdateStage.START, startStage);
        this.createStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.createStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.createStages.put(UpdateStage.UPDATE_DATA_STORE, createStage);
        this.createStages.put(UpdateStage.FILL_RESPONSE, createdResponseStage);

        this.updateStages = new EnumMap<>(UpdateStage.class);
        this.updateStages.put(UpdateStage.START, startStage);
        this.updateStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.updateStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.updateStages.put(UpdateStage.UPDATE_DATA_STORE, updateStage);
        this.updateStages.put(UpdateStage.FILL_RESPONSE, okResponseStage);

        this.createOrUpdateStages = new EnumMap<>(UpdateStage.class);
        this.createOrUpdateStages.put(UpdateStage.START, startStage);
        this.createOrUpdateStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.createOrUpdateStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.createOrUpdateStages.put(UpdateStage.UPDATE_DATA_STORE, createOrUpdateStage);
        this.createOrUpdateStages.put(UpdateStage.FILL_RESPONSE, okResponseStage);

        this.idempotentCreateOrUpdateStages = new EnumMap<>(UpdateStage.class);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.START, startStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.UPDATE_DATA_STORE, idempotentCreateOrUpdateStage);
        this.idempotentCreateOrUpdateStages.put(UpdateStage.FILL_RESPONSE, okResponseStage);

        this.idempotentFullSyncStages = new EnumMap<>(UpdateStage.class);
        this.idempotentFullSyncStages.put(UpdateStage.START, startStage);
        this.idempotentFullSyncStages.put(UpdateStage.PARSE_REQUEST, parseRequestStage);
        this.idempotentFullSyncStages.put(UpdateStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        this.idempotentFullSyncStages.put(UpdateStage.UPDATE_DATA_STORE, idempotentFullSyncStage);
        this.idempotentFullSyncStages.put(UpdateStage.FILL_RESPONSE, okResponseStage);
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
