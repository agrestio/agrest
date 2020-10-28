package io.agrest.cayenne;

import io.agrest.base.jsonvalueconverter.JsonValueConverter;
import io.agrest.cayenne.compiler.CayenneEntityCompiler;
import io.agrest.cayenne.converter.JsonConverter;
import io.agrest.cayenne.encoder.JsonEncoder;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneQueryAssembler;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.cayenne.processor.delete.CayenneDeleteProcessorFactoryProvider;
import io.agrest.cayenne.processor.delete.CayenneDeleteStage;
import io.agrest.cayenne.processor.delete.CayenneDeleteStartStage;
import io.agrest.cayenne.processor.unrelate.CayenneUnrelateDataStoreStage;
import io.agrest.cayenne.processor.unrelate.CayenneUnrelateProcessorFactoryProvider;
import io.agrest.cayenne.processor.unrelate.CayenneUnrelateStartStage;
import io.agrest.cayenne.processor.update.*;
import io.agrest.cayenne.provider.CayenneRuntimeExceptionMapper;
import io.agrest.cayenne.provider.ValidationExceptionMapper;
import io.agrest.cayenne.qualifier.IQualifierParser;
import io.agrest.cayenne.qualifier.IQualifierPostProcessor;
import io.agrest.cayenne.qualifier.QualifierParser;
import io.agrest.cayenne.qualifier.QualifierPostProcessor;
import io.agrest.encoder.Encoder;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;
import io.agrest.runtime.processor.update.UpdateProcessorFactoryFactory;
import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.validation.ValidationException;
import org.apache.cayenne.value.Json;

import javax.ws.rs.ext.ExceptionMapper;
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

        binder.bind(CayenneEntityCompiler.class).to(CayenneEntityCompiler.class);
        binder.bindList(AgEntityCompiler.class).insertBefore(CayenneEntityCompiler.class, PojoEntityCompiler.class);
        binder.bind(ICayennePersister.class).toInstance(persister);
        binder.bind(IQualifierParser.class).to(QualifierParser.class);
        binder.bind(IQualifierPostProcessor.class).to(QualifierPostProcessor.class);
        binder.bind(ICayenneQueryAssembler.class).to(CayenneQueryAssembler.class);

        // delete stages
        binder.bind(DeleteProcessorFactory.class).toProvider(CayenneDeleteProcessorFactoryProvider.class);
        binder.bind(CayenneDeleteStartStage.class).to(CayenneDeleteStartStage.class);
        binder.bind(CayenneDeleteStage.class).to(CayenneDeleteStage.class);

        // update stages
        binder.bind(UpdateProcessorFactoryFactory.class).toProvider(CayenneUpdateProcessorFactoryFactoryProvider.class);
        binder.bind(CayenneUpdateStartStage.class).to(CayenneUpdateStartStage.class);
        binder.bind(CayenneApplyServerParamsStage.class).to(CayenneApplyServerParamsStage.class);
        binder.bind(CayenneCreateStage.class).to(CayenneCreateStage.class);
        binder.bind(CayenneUpdateStage.class).to(CayenneUpdateStage.class);
        binder.bind(CayenneCreateOrUpdateStage.class).to(CayenneCreateOrUpdateStage.class);
        binder.bind(CayenneIdempotentCreateOrUpdateStage.class).to(CayenneIdempotentCreateOrUpdateStage.class);
        binder.bind(CayenneIdempotentFullSyncStage.class).to(CayenneIdempotentFullSyncStage.class);
        binder.bind(CayenneCommitStage.class).to(CayenneCommitStage.class);
        binder.bind(CayenneOkResponseStage.class).to(CayenneOkResponseStage.class);
        binder.bind(CayenneCreatedResponseStage.class).to(CayenneCreatedResponseStage.class);

        binder.bindMap(ExceptionMapper.class)
                .put(CayenneRuntimeException.class.getName(), CayenneRuntimeExceptionMapper.class)
                .put(ValidationException.class.getName(), ValidationExceptionMapper.class);

        // unrelate stages
        binder.bind(UnrelateProcessorFactory.class).toProvider(CayenneUnrelateProcessorFactoryProvider.class);
        binder.bind(CayenneUnrelateStartStage.class).to(CayenneUnrelateStartStage.class);
        binder.bind(CayenneUnrelateDataStoreStage.class).to(CayenneUnrelateDataStoreStage.class);
    }
}
