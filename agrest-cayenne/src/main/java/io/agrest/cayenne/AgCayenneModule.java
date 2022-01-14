package io.agrest.cayenne;

import io.agrest.cayenne.compiler.CayenneAgEntityCompiler;
import io.agrest.cayenne.converter.JsonConverter;
import io.agrest.cayenne.encoder.JsonEncoder;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.path.PathResolver;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneQueryAssembler;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.cayenne.processor.delete.CayenneDeleteProcessorFactoryProvider;
import io.agrest.cayenne.processor.delete.CayenneDeleteStage;
import io.agrest.cayenne.processor.delete.CayenneDeleteStartStage;
import io.agrest.cayenne.processor.unrelate.CayenneUnrelateDataStoreStage;
import io.agrest.cayenne.processor.unrelate.CayenneUnrelateProcessorFactoryProvider;
import io.agrest.cayenne.processor.unrelate.CayenneUnrelateStartStage;
import io.agrest.cayenne.processor.update.CayenneApplyServerParamsStage;
import io.agrest.cayenne.processor.update.CayenneCommitStage;
import io.agrest.cayenne.processor.update.CayenneCreatedOrOkResponseStage;
import io.agrest.cayenne.processor.update.CayenneCreatedResponseStage;
import io.agrest.cayenne.processor.update.CayenneMapCreateOrUpdateStage;
import io.agrest.cayenne.processor.update.CayenneMapCreateStage;
import io.agrest.cayenne.processor.update.CayenneMapIdempotentCreateOrUpdateStage;
import io.agrest.cayenne.processor.update.CayenneMapIdempotentFullSyncStage;
import io.agrest.cayenne.processor.update.CayenneMapUpdateStage;
import io.agrest.cayenne.processor.update.CayenneMergeChangesStage;
import io.agrest.cayenne.processor.update.CayenneOkResponseStage;
import io.agrest.cayenne.processor.update.CayenneUpdateProcessorFactoryFactoryProvider;
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
import io.agrest.encoder.Encoder;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.select.ApplyServerParamsStage;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;
import io.agrest.runtime.processor.update.UpdateProcessorFactoryFactory;
import io.agrest.spi.AgExceptionMapper;
import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.di.Binder;
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
        binder.bindMap(Encoder.class).put(Json.class.getName(), JsonEncoder.encoder());
        binder.bindMap(JsonValueConverter.class).put(Json.class.getName(), JsonConverter.converter());

        binder.bind(CayenneAgEntityCompiler.class).to(CayenneAgEntityCompiler.class);
        binder.bindList(AgEntityCompiler.class).insertBefore(CayenneAgEntityCompiler.class, AnnotationsAgEntityCompiler.class);
        binder.bind(ICayennePersister.class).toInstance(persister);
        binder.bind(IQualifierParser.class).to(QualifierParser.class);
        binder.bind(IQualifierPostProcessor.class).to(QualifierPostProcessor.class);
        binder.bind(ICayenneQueryAssembler.class).to(CayenneQueryAssembler.class);
        binder.bind(IPathResolver.class).to(PathResolver.class);

        // select stages
        binder.bind(ApplyServerParamsStage.class).to(io.agrest.cayenne.processor.select.CayenneApplyServerParamsStage.class);

        // delete stages
        binder.bind(DeleteProcessorFactory.class).toProvider(CayenneDeleteProcessorFactoryProvider.class);
        binder.bind(CayenneDeleteStartStage.class).to(CayenneDeleteStartStage.class);
        binder.bind(io.agrest.cayenne.processor.delete.CayenneMapChangesStage.class).to(io.agrest.cayenne.processor.delete.CayenneMapChangesStage.class);
        binder.bind(CayenneDeleteStage.class).to(CayenneDeleteStage.class);

        // update stages
        binder.bind(UpdateProcessorFactoryFactory.class).toProvider(CayenneUpdateProcessorFactoryFactoryProvider.class);
        binder.bind(CayenneUpdateStartStage.class).to(CayenneUpdateStartStage.class);
        binder.bind(CayenneApplyServerParamsStage.class).to(CayenneApplyServerParamsStage.class);
        binder.bind(CayenneMapCreateStage.class).to(CayenneMapCreateStage.class);
        binder.bind(CayenneMapUpdateStage.class).to(CayenneMapUpdateStage.class);
        binder.bind(CayenneMapCreateOrUpdateStage.class).to(CayenneMapCreateOrUpdateStage.class);
        binder.bind(CayenneMapIdempotentCreateOrUpdateStage.class).to(CayenneMapIdempotentCreateOrUpdateStage.class);
        binder.bind(CayenneMapIdempotentFullSyncStage.class).to(CayenneMapIdempotentFullSyncStage.class);
        binder.bind(CayenneMergeChangesStage.class).to(CayenneMergeChangesStage.class);
        binder.bind(CayenneCommitStage.class).to(CayenneCommitStage.class);
        binder.bind(CayenneOkResponseStage.class).to(CayenneOkResponseStage.class);
        binder.bind(CayenneCreatedResponseStage.class).to(CayenneCreatedResponseStage.class);
        binder.bind(CayenneCreatedOrOkResponseStage.class).to(CayenneCreatedOrOkResponseStage.class);

        binder.bindMap(AgExceptionMapper.class)
                .put(CayenneRuntimeException.class.getName(), CayenneRuntimeExceptionMapper.class)
                .put(ValidationException.class.getName(), ValidationExceptionMapper.class);

        // unrelate stages
        binder.bind(UnrelateProcessorFactory.class).toProvider(CayenneUnrelateProcessorFactoryProvider.class);
        binder.bind(CayenneUnrelateStartStage.class).to(CayenneUnrelateStartStage.class);
        binder.bind(CayenneUnrelateDataStoreStage.class).to(CayenneUnrelateDataStoreStage.class);
    }
}
