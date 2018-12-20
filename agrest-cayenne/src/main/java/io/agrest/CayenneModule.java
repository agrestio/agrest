package io.agrest;

import io.agrest.backend.util.converter.ExpressionMatcher;
import io.agrest.backend.util.converter.OrderingConverter;
import io.agrest.backend.util.converter.OrderingSorter;
import io.agrest.meta.compiler.CayenneEntityCompiler;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.provider.AgExceptionMapper;
import io.agrest.provider.CayenneRuntimeExceptionMapper;
import io.agrest.provider.ValidationExceptionMapper;
import io.agrest.runtime.cayenne.converter.CayenneExpressionMatcher;
import io.agrest.runtime.cayenne.converter.CayenneOrderingConverter;
import io.agrest.runtime.cayenne.converter.CayenneOrderingSorter;
import io.agrest.runtime.cayenne.processor.delete.CayenneDeleteProcessorFactoryProvider;
import io.agrest.runtime.cayenne.processor.delete.CayenneDeleteStage;
import io.agrest.runtime.cayenne.processor.delete.CayenneDeleteStartStage;
import io.agrest.runtime.cayenne.processor.select.CayenneAssembleQueryStage;
import io.agrest.runtime.cayenne.processor.select.CayenneFetchDataStage;
import io.agrest.runtime.cayenne.processor.select.CayenneSelectProcessorFactoryProvider;
import io.agrest.runtime.cayenne.processor.unrelate.CayenneUnrelateDataStoreStage;
import io.agrest.runtime.cayenne.processor.unrelate.CayenneUnrelateProcessorFactoryProvider;
import io.agrest.runtime.cayenne.processor.unrelate.CayenneUnrelateStartStage;
import io.agrest.runtime.cayenne.processor.update.CayenneCreateOrUpdateStage;
import io.agrest.runtime.cayenne.processor.update.CayenneCreateStage;
import io.agrest.runtime.cayenne.processor.update.CayenneCreatedResponseStage;
import io.agrest.runtime.cayenne.processor.update.CayenneIdempotentCreateOrUpdateStage;
import io.agrest.runtime.cayenne.processor.update.CayenneIdempotentFullSyncStage;
import io.agrest.runtime.cayenne.processor.update.CayenneOkResponseStage;
import io.agrest.runtime.cayenne.processor.update.CayenneUpdateProcessorFactoryFactoryProvider;
import io.agrest.runtime.cayenne.processor.update.CayenneUpdateStage;
import io.agrest.runtime.cayenne.processor.update.CayenneUpdateStartStage;
import io.agrest.runtime.encoder.AttributeEncoderFactoryProvider;
import io.agrest.runtime.encoder.IAttributeEncoderFactory;
import io.agrest.runtime.entity.CayenneExpMerger;
import io.agrest.runtime.entity.ExpressionPostProcessor;
import io.agrest.runtime.entity.IAgExpMerger;
import io.agrest.runtime.entity.IExpressionPostProcessor;
import io.agrest.runtime.entity.ISortMerger;
import io.agrest.runtime.entity.SortMerger;
import io.agrest.runtime.path.IPathDescriptorManager;
import io.agrest.runtime.path.PathDescriptorManager;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;
import io.agrest.runtime.processor.update.UpdateProcessorFactoryFactory;
import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.MapBuilder;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.validation.ValidationException;

import javax.ws.rs.ext.ExceptionMapper;

/**
 *
 *
 */
public class CayenneModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(CayenneEntityCompiler.class).to(CayenneEntityCompiler.class);

        binder.bindList(AgEntityCompiler.class)
                .add(CayenneEntityCompiler.class)
                .add(PojoEntityCompiler.class);

        MapBuilder<ExceptionMapper> mapperBuilder = binder.bindMap(ExceptionMapper.class)
                .put(CayenneRuntimeException.class.getName(), CayenneRuntimeExceptionMapper.class)
                .put(AgException.class.getName(), AgExceptionMapper.class)
                .put(ValidationException.class.getName(), ValidationExceptionMapper.class);


        // select stages
        binder.bind(SelectProcessorFactory.class).toProvider(CayenneSelectProcessorFactoryProvider.class);
        binder.bind(CayenneAssembleQueryStage.class).to(CayenneAssembleQueryStage.class);
        binder.bind(CayenneFetchDataStage.class).to(CayenneFetchDataStage.class);

        // delete stages
        binder.bind(DeleteProcessorFactory.class).toProvider(CayenneDeleteProcessorFactoryProvider.class);
        binder.bind(CayenneDeleteStartStage.class).to(CayenneDeleteStartStage.class);
        binder.bind(CayenneDeleteStage.class).to(CayenneDeleteStage.class);

        // update stages
        binder.bind(UpdateProcessorFactoryFactory.class)
                .toProvider(CayenneUpdateProcessorFactoryFactoryProvider.class);
        binder.bind(CayenneUpdateStartStage.class).to(CayenneUpdateStartStage.class);
        binder.bind(CayenneCreateStage.class).to(CayenneCreateStage.class);
        binder.bind(CayenneUpdateStage.class).to(CayenneUpdateStage.class);
        binder.bind(CayenneCreateOrUpdateStage.class).to(CayenneCreateOrUpdateStage.class);
        binder.bind(CayenneIdempotentCreateOrUpdateStage.class).to(CayenneIdempotentCreateOrUpdateStage.class);
        binder.bind(CayenneIdempotentFullSyncStage.class).to(CayenneIdempotentFullSyncStage.class);
        binder.bind(CayenneOkResponseStage.class).to(CayenneOkResponseStage.class);
        binder.bind(CayenneCreatedResponseStage.class).to(CayenneCreatedResponseStage.class);

        // unrelate stages
        binder.bind(UnrelateProcessorFactory.class).toProvider(CayenneUnrelateProcessorFactoryProvider.class);
        binder.bind(CayenneUnrelateStartStage.class).to(CayenneUnrelateStartStage.class);
        binder.bind(CayenneUnrelateDataStoreStage.class).to(CayenneUnrelateDataStoreStage.class);

        // a map of custom encoders
        binder.bind(IAttributeEncoderFactory.class).toProvider(AttributeEncoderFactoryProvider.class);

        // a map of custom converters
        binder.bind(IExpressionPostProcessor.class).to(ExpressionPostProcessor.class);

        binder.bind(IPathDescriptorManager.class).to(PathDescriptorManager.class);

        // Backend converters and matchers
        binder.bind(ExpressionMatcher.class).to(CayenneExpressionMatcher.class);
        binder.bind(OrderingConverter.class).to(CayenneOrderingConverter.class);
        binder.bind(OrderingSorter.class).to(CayenneOrderingSorter.class);

        // Constructors to create ResourceEntity from Query parameters
        binder.bind(IAgExpMerger.class).to(CayenneExpMerger.class);
        binder.bind(ISortMerger.class).to(SortMerger.class);
    }
}
