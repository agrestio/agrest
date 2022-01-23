package io.agrest.cayenne;

import io.agrest.cayenne.compiler.CayenneAgEntityCompiler;
import io.agrest.cayenne.encoder.JsonEncoder;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.path.PathResolver;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneQueryAssembler;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.cayenne.processor.delete.CayenneDeleteInDataStoreStage;
import io.agrest.cayenne.processor.delete.CayenneDeleteStartStage;
import io.agrest.cayenne.processor.unrelate.CayenneUnrelateDataStoreStage;
import io.agrest.cayenne.processor.unrelate.CayenneUnrelateStartStage;
import io.agrest.cayenne.processor.update.CayenneCreatedOrOkResponseStage;
import io.agrest.cayenne.processor.update.CayenneCreatedResponseStage;
import io.agrest.cayenne.processor.update.CayenneMapCreateOrUpdateStage;
import io.agrest.cayenne.processor.update.CayenneMapCreateStage;
import io.agrest.cayenne.processor.update.CayenneMapIdempotentCreateOrUpdateStage;
import io.agrest.cayenne.processor.update.CayenneMapIdempotentFullSyncStage;
import io.agrest.cayenne.processor.update.CayenneMapUpdateStage;
import io.agrest.cayenne.processor.update.CayenneMergeChangesStage;
import io.agrest.cayenne.processor.update.CayenneOkResponseStage;
import io.agrest.cayenne.processor.update.CayenneUpdateApplyServerParamsStage;
import io.agrest.cayenne.processor.update.CayenneUpdateCommitStage;
import io.agrest.cayenne.processor.update.CayenneUpdateStartStage;
import io.agrest.cayenne.provider.CayenneRuntimeExceptionMapper;
import io.agrest.cayenne.provider.ValidationExceptionMapper;
import io.agrest.cayenne.qualifier.IQualifierParser;
import io.agrest.cayenne.qualifier.IQualifierPostProcessor;
import io.agrest.cayenne.qualifier.QualifierParser;
import io.agrest.cayenne.qualifier.QualifierPostProcessor;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.converter.jsonvalue.JsonValueConverter;
import io.agrest.converter.valuestring.ValueStringConverter;
import io.agrest.encoder.Encoder;
import io.agrest.runtime.processor.delete.DeleteInDataStoreStage;
import io.agrest.runtime.processor.delete.DeleteMapChangesStage;
import io.agrest.runtime.processor.delete.DeleteStartStage;
import io.agrest.runtime.processor.select.ApplyServerParamsStage;
import io.agrest.runtime.processor.unrelate.UnrelateStartStage;
import io.agrest.runtime.processor.unrelate.UnrelateUpdateDateStoreStage;
import io.agrest.runtime.processor.update.UpdateFlavorDIKeys;
import io.agrest.runtime.processor.update.stage.UpdateApplyServerParamsStage;
import io.agrest.runtime.processor.update.stage.UpdateCommitStage;
import io.agrest.runtime.processor.update.stage.UpdateFillResponseStage;
import io.agrest.runtime.processor.update.stage.UpdateMapChangesStage;
import io.agrest.runtime.processor.update.stage.UpdateMergeChangesStage;
import io.agrest.runtime.processor.update.stage.UpdateStartStage;
import io.agrest.spi.AgExceptionMapper;
import org.apache.cayenne.CayenneRuntimeException;
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
        binder.bind(IQualifierParser.class).to(QualifierParser.class);
        binder.bind(IQualifierPostProcessor.class).to(QualifierPostProcessor.class);
        binder.bind(ICayenneQueryAssembler.class).to(CayenneQueryAssembler.class);
        binder.bind(IPathResolver.class).to(PathResolver.class);

        binder.bindMap(AgExceptionMapper.class)
                .put(CayenneRuntimeException.class.getName(), CayenneRuntimeExceptionMapper.class)
                .put(ValidationException.class.getName(), ValidationExceptionMapper.class);

        // Cayenne overrides for select stages
        binder.bind(ApplyServerParamsStage.class).to(io.agrest.cayenne.processor.select.CayenneApplyServerParamsStage.class);
        
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
        binder.bind(DeleteMapChangesStage.class).to(io.agrest.cayenne.processor.delete.CayenneMapChangesStage.class);
        binder.bind(DeleteInDataStoreStage.class).to(CayenneDeleteInDataStoreStage.class);

        // Cayenne overrides for unrelate stages
        binder.bind(UnrelateStartStage.class).to(CayenneUnrelateStartStage.class);
        binder.bind(UnrelateUpdateDateStoreStage.class).to(CayenneUnrelateDataStoreStage.class);
    }
}
