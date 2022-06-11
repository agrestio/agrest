package io.agrest.cayenne;

import io.agrest.cayenne.compiler.CayenneAgEntityCompiler;
import io.agrest.cayenne.encoder.JsonEncoder;
import io.agrest.cayenne.exp.CayenneExpParser;
import io.agrest.cayenne.exp.CayenneExpPostProcessor;
import io.agrest.cayenne.exp.ICayenneExpParser;
import io.agrest.cayenne.exp.ICayenneExpPostProcessor;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.path.PathResolver;
import io.agrest.cayenne.persister.CayennePersister;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneQueryAssembler;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.cayenne.processor.delete.stage.CayenneDeleteInDataStoreStage;
import io.agrest.cayenne.processor.delete.stage.CayenneDeleteMapChangesStage;
import io.agrest.cayenne.processor.delete.stage.CayenneDeleteStartStage;
import io.agrest.cayenne.processor.select.stage.CayenneSelectApplyServerParamsStage;
import io.agrest.cayenne.processor.unrelate.stage.CayenneUnrelateDataStoreStage;
import io.agrest.cayenne.processor.unrelate.stage.CayenneUnrelateStartStage;
import io.agrest.cayenne.processor.update.stage.CayenneCreatedOrOkResponseStage;
import io.agrest.cayenne.processor.update.stage.CayenneCreatedResponseStage;
import io.agrest.cayenne.processor.update.stage.CayenneMapCreateOrUpdateStage;
import io.agrest.cayenne.processor.update.stage.CayenneMapCreateStage;
import io.agrest.cayenne.processor.update.stage.CayenneMapIdempotentCreateOrUpdateStage;
import io.agrest.cayenne.processor.update.stage.CayenneMapIdempotentFullSyncStage;
import io.agrest.cayenne.processor.update.stage.CayenneMapUpdateStage;
import io.agrest.cayenne.processor.update.stage.CayenneMergeChangesStage;
import io.agrest.cayenne.processor.update.stage.CayenneOkResponseStage;
import io.agrest.cayenne.processor.update.stage.CayenneUpdateApplyServerParamsStage;
import io.agrest.cayenne.processor.update.stage.CayenneUpdateCommitStage;
import io.agrest.cayenne.processor.update.stage.CayenneUpdateStartStage;
import io.agrest.cayenne.spi.CayenneRuntimeExceptionMapper;
import io.agrest.cayenne.spi.ValidationExceptionMapper;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.converter.jsonvalue.JsonValueConverter;
import io.agrest.converter.valuestring.ValueStringConverter;
import io.agrest.encoder.Encoder;
import io.agrest.runtime.processor.delete.stage.DeleteInDataStoreStage;
import io.agrest.runtime.processor.delete.stage.DeleteMapChangesStage;
import io.agrest.runtime.processor.delete.stage.DeleteStartStage;
import io.agrest.runtime.processor.select.stage.SelectApplyServerParamsStage;
import io.agrest.runtime.processor.unrelate.stage.UnrelateStartStage;
import io.agrest.runtime.processor.unrelate.stage.UnrelateUpdateDateStoreStage;
import io.agrest.runtime.processor.update.UpdateFlavorDIKeys;
import io.agrest.runtime.processor.update.stage.UpdateApplyServerParamsStage;
import io.agrest.runtime.processor.update.stage.UpdateCommitStage;
import io.agrest.runtime.processor.update.stage.UpdateFillResponseStage;
import io.agrest.runtime.processor.update.stage.UpdateMapChangesStage;
import io.agrest.runtime.processor.update.stage.UpdateMergeChangesStage;
import io.agrest.runtime.processor.update.stage.UpdateStartStage;
import io.agrest.spi.AgExceptionMapper;
import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Key;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.validation.ValidationException;
import org.apache.cayenne.value.Json;

import java.util.Objects;

/**
 * @since 3.4
 */
public class AgCayenneModule implements Module {

    private final ICayennePersister persister;

    /**
     * A shortcut that creates a Agrest Cayenne extension based on Cayenne runtime and default settings.
     */
    public static AgCayenneModule build(ServerRuntime cayenneRuntime) {
        return builder(cayenneRuntime).build();
    }

    /**
     * A shortcut that creates an AgCayenneBuilder, setting its Cayenne runtime. The caller can continue customizing
     * the returned builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ServerRuntime cayenneRuntime) {
        return builder().runtime(cayenneRuntime);
    }

    public AgCayenneModule(ICayennePersister persister) {
        this.persister = Objects.requireNonNull(persister);
    }

    @Override
    public void configure(Binder binder) {
        binder.bindMap(JsonValueConverter.class).put(Json.class.getName(), io.agrest.cayenne.converter.jsonvalue.JsonConverter.converter());
        binder.bindMap(ValueStringConverter.class).put(Json.class.getName(), io.agrest.cayenne.converter.valuestring.JsonConverter.converter());

        // Despite the presence of "io.agrest.cayenne.converter.valuestring.JsonConverter", we still need an explicit
        // JsonEncoder, as unlike a generic ValueEncoder it must write its content with no escaping
        binder.bindMap(Encoder.class).put(Json.class.getName(), JsonEncoder.encoder());

        binder.bind(CayenneAgEntityCompiler.class).to(CayenneAgEntityCompiler.class);
        binder.bindList(AgEntityCompiler.class).insertBefore(CayenneAgEntityCompiler.class, AnnotationsAgEntityCompiler.class);
        binder.bind(ICayennePersister.class).toInstance(persister);
        binder.bind(ICayenneExpParser.class).to(CayenneExpParser.class);
        binder.bind(ICayenneExpPostProcessor.class).to(CayenneExpPostProcessor.class);
        binder.bind(ICayenneQueryAssembler.class).to(CayenneQueryAssembler.class);
        binder.bind(IPathResolver.class).to(PathResolver.class);

        binder.bindMap(AgExceptionMapper.class)
                .put(CayenneRuntimeException.class.getName(), CayenneRuntimeExceptionMapper.class)
                .put(ValidationException.class.getName(), ValidationExceptionMapper.class);

        // Cayenne overrides for select stages
        binder.bind(SelectApplyServerParamsStage.class).to(CayenneSelectApplyServerParamsStage.class);
        
        // Cayenne overrides for update stages
        binder.bind(UpdateStartStage.class).to(CayenneUpdateStartStage.class);
        binder.bind(UpdateApplyServerParamsStage.class).to(CayenneUpdateApplyServerParamsStage.class);
        binder.bind(UpdateMergeChangesStage.class).to(CayenneMergeChangesStage.class);
        binder.bind(UpdateCommitStage.class).to(CayenneUpdateCommitStage.class);

        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.CREATE)).to(CayenneMapCreateStage.class);
        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.CREATE_OR_UPDATE)).to(CayenneMapCreateOrUpdateStage.class);
        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.IDEMPOTENT_CREATE_OR_UPDATE)).to(CayenneMapIdempotentCreateOrUpdateStage.class);
        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.IDEMPOTENT_FULL_SYNC)).to(CayenneMapIdempotentFullSyncStage.class);
        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.UPDATE)).to(CayenneMapUpdateStage.class);

        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.CREATE)).to(CayenneCreatedResponseStage.class);
        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.CREATE_OR_UPDATE)).to(CayenneCreatedOrOkResponseStage.class);
        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.IDEMPOTENT_CREATE_OR_UPDATE)).to(CayenneCreatedOrOkResponseStage.class);
        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.IDEMPOTENT_FULL_SYNC)).to(CayenneCreatedOrOkResponseStage.class);
        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.UPDATE)).to(CayenneOkResponseStage.class);

        // Cayenne overrides for delete stages
        binder.bind(DeleteStartStage.class).to(CayenneDeleteStartStage.class);
        binder.bind(DeleteMapChangesStage.class).to(CayenneDeleteMapChangesStage.class);
        binder.bind(DeleteInDataStoreStage.class).to(CayenneDeleteInDataStoreStage.class);

        // Cayenne overrides for unrelate stages
        binder.bind(UnrelateStartStage.class).to(CayenneUnrelateStartStage.class);
        binder.bind(UnrelateUpdateDateStoreStage.class).to(CayenneUnrelateDataStoreStage.class);
    }

    public static class Builder {

        private ICayennePersister persister;

        private Builder() {
        }

        public Builder runtime(ServerRuntime cayenneRuntime) {
            this.persister = new CayennePersister(cayenneRuntime);
            return this;
        }

        public Builder persister(ICayennePersister persister) {
            this.persister = persister;
            return this;
        }

        public AgCayenneModule build() {
            return new AgCayenneModule(persister);
        }
    }
}
