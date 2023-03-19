package io.agrest.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.access.PathChecker;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.converter.jsonvalue.Base64Converter;
import io.agrest.converter.jsonvalue.BigDecimalConverter;
import io.agrest.converter.jsonvalue.DoubleConverter;
import io.agrest.converter.jsonvalue.FloatConverter;
import io.agrest.converter.jsonvalue.GenericConverter;
import io.agrest.converter.jsonvalue.JsonNodeConverter;
import io.agrest.converter.jsonvalue.JsonValueConverter;
import io.agrest.converter.jsonvalue.JsonValueConverters;
import io.agrest.converter.jsonvalue.JsonValueConvertersProvider;
import io.agrest.converter.jsonvalue.LongConverter;
import io.agrest.converter.jsonvalue.UtcDateConverter;
import io.agrest.converter.valuestring.SqlDateConverter;
import io.agrest.converter.valuestring.SqlTimestampConverter;
import io.agrest.converter.valuestring.LocalDateConverter;
import io.agrest.converter.valuestring.LocalDateTimeConverter;
import io.agrest.converter.valuestring.LocalTimeConverter;
import io.agrest.converter.valuestring.OffsetDateTimeConverter;
import io.agrest.converter.valuestring.SqlTimeConverter;
import io.agrest.converter.valuestring.UtilDateConverter;
import io.agrest.converter.valuestring.ValueStringConverter;
import io.agrest.converter.valuestring.ValueStringConverters;
import io.agrest.converter.valuestring.ValueStringConvertersProvider;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncodingPolicy;
import io.agrest.encoder.ValueEncoders;
import io.agrest.encoder.ValueEncodersProvider;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgSchema;
import io.agrest.runtime.constraints.ConstraintsHandler;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.encoder.EncoderFactory;
import io.agrest.runtime.encoder.EncodablePropertyFactory;
import io.agrest.runtime.encoder.IEncodablePropertyFactory;
import io.agrest.runtime.entity.ChangeAuthorizer;
import io.agrest.runtime.entity.ExcludeMerger;
import io.agrest.runtime.entity.ExpMerger;
import io.agrest.runtime.entity.IChangeAuthorizer;
import io.agrest.runtime.entity.IExcludeMerger;
import io.agrest.runtime.entity.IExpMerger;
import io.agrest.runtime.entity.IIncludeMerger;
import io.agrest.runtime.entity.IMapByMerger;
import io.agrest.runtime.entity.IResultFilter;
import io.agrest.runtime.entity.ISizeMerger;
import io.agrest.runtime.entity.ISortMerger;
import io.agrest.runtime.entity.IncludeMerger;
import io.agrest.runtime.entity.MapByMerger;
import io.agrest.runtime.entity.ResultFilter;
import io.agrest.runtime.entity.SizeMerger;
import io.agrest.runtime.entity.SortMerger;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JacksonServiceProvider;
import io.agrest.runtime.meta.LazySchemaProvider;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.delete.provider.DeleteProcessorFactoryProvider;
import io.agrest.runtime.processor.delete.stage.DeleteAuthorizeChangesStage;
import io.agrest.runtime.processor.delete.stage.DeleteInDataStoreStage;
import io.agrest.runtime.processor.delete.stage.DeleteMapChangesStage;
import io.agrest.runtime.processor.delete.stage.DeleteStartStage;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import io.agrest.runtime.processor.select.provider.SelectProcessorFactoryProvider;
import io.agrest.runtime.processor.select.stage.SelectApplyServerParamsStage;
import io.agrest.runtime.processor.select.stage.SelectAssembleQueryStage;
import io.agrest.runtime.processor.select.stage.SelectCreateResourceEntityStage;
import io.agrest.runtime.processor.select.stage.SelectEncoderInstallStage;
import io.agrest.runtime.processor.select.stage.SelectFetchDataStage;
import io.agrest.runtime.processor.select.stage.SelectFilterResultStage;
import io.agrest.runtime.processor.select.stage.SelectStartStage;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;
import io.agrest.runtime.processor.unrelate.provider.UnrelateProcessorFactoryProvider;
import io.agrest.runtime.processor.unrelate.stage.UnrelateStartStage;
import io.agrest.runtime.processor.unrelate.stage.UnrelateUpdateDateStoreStage;
import io.agrest.runtime.processor.update.CreateOrUpdateProcessorFactory;
import io.agrest.runtime.processor.update.CreateProcessorFactory;
import io.agrest.runtime.processor.update.IdempotentCreateOrUpdateProcessorFactory;
import io.agrest.runtime.processor.update.IdempotentFullSyncProcessorFactory;
import io.agrest.runtime.processor.update.UpdateFlavorDIKeys;
import io.agrest.runtime.processor.update.UpdateProcessorFactory;
import io.agrest.runtime.processor.update.provider.CreateOrUpdateProcessorFactoryProvider;
import io.agrest.runtime.processor.update.provider.CreateProcessorFactoryProvider;
import io.agrest.runtime.processor.update.provider.IdempotentCreateOrUpdateProcessorFactoryProvider;
import io.agrest.runtime.processor.update.provider.IdempotentFullSyncProcessorFactoryProvider;
import io.agrest.runtime.processor.update.provider.UpdateProcessorFactoryProvider;
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
import io.agrest.runtime.protocol.EntityUpdateParser;
import io.agrest.runtime.protocol.ExcludeParser;
import io.agrest.runtime.protocol.ExpParser;
import io.agrest.runtime.protocol.IEntityUpdateParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IExpParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISizeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.protocol.IncludeParser;
import io.agrest.runtime.protocol.SizeParser;
import io.agrest.runtime.protocol.SortParser;
import io.agrest.runtime.request.DefaultRequestBuilderFactory;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import io.agrest.runtime.semantics.IRelationshipMapper;
import io.agrest.runtime.semantics.RelationshipMapper;
import io.agrest.spi.AgExceptionDefaultMapper;
import io.agrest.spi.AgExceptionMapper;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Key;
import org.apache.cayenne.di.Module;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Configures core Agrest services.
 *
 * @since 5.0
 */
public class AgCoreModule implements Module {

    private final Map<String, AgEntityOverlay> entityOverlays;
    private final PathChecker pathChecker;
    private final EncodingPolicy encodingPolicy;

    protected AgCoreModule(
            Map<String, AgEntityOverlay> entityOverlays,
            PathChecker pathChecker,
            EncodingPolicy encodingPolicy) {
        this.entityOverlays = Objects.requireNonNull(entityOverlays);
        this.pathChecker = Objects.requireNonNull(pathChecker);
        this.encodingPolicy = Objects.requireNonNull(encodingPolicy);
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(AnnotationsAgEntityCompiler.class).to(AnnotationsAgEntityCompiler.class);
        binder.bindList(AgEntityCompiler.class).add(AnnotationsAgEntityCompiler.class);

        binder.bindMap(AgEntityOverlay.class).putAll(entityOverlays);
        binder.bind(PathChecker.class).toInstance(pathChecker);
        binder.bind(EncodingPolicy.class).toInstance(encodingPolicy);

        binder.bindMap(AgExceptionMapper.class)
                .put(AgException.class.getName(), AgExceptionDefaultMapper.class);

        binder.bind(AgExceptionMappers.class).to(AgExceptionMappers.class);

        // select stages
        binder.bind(SelectProcessorFactory.class).toProvider(SelectProcessorFactoryProvider.class);
        binder.bind(SelectStartStage.class).to(SelectStartStage.class);
        binder.bind(SelectCreateResourceEntityStage.class).to(SelectCreateResourceEntityStage.class);
        binder.bind(SelectApplyServerParamsStage.class).to(SelectApplyServerParamsStage.class);
        binder.bind(SelectEncoderInstallStage.class).to(SelectEncoderInstallStage.class);
        binder.bind(SelectAssembleQueryStage.class).to(SelectAssembleQueryStage.class);
        binder.bind(SelectFetchDataStage.class).to(SelectFetchDataStage.class);
        binder.bind(SelectFilterResultStage.class).to(SelectFilterResultStage.class);

        // update stages
        binder.bind(CreateProcessorFactory.class).toProvider(CreateProcessorFactoryProvider.class);
        binder.bind(UpdateProcessorFactory.class).toProvider(UpdateProcessorFactoryProvider.class);
        binder.bind(CreateOrUpdateProcessorFactory.class).toProvider(CreateOrUpdateProcessorFactoryProvider.class);
        binder.bind(IdempotentCreateOrUpdateProcessorFactory.class).toProvider(IdempotentCreateOrUpdateProcessorFactoryProvider.class);
        binder.bind(IdempotentFullSyncProcessorFactory.class).toProvider(IdempotentFullSyncProcessorFactoryProvider.class);

        binder.bind(UpdateStartStage.class).to(UpdateStartStage.class);
        binder.bind(UpdateApplyServerParamsStage.class).to(UpdateApplyServerParamsStage.class);
        binder.bind(UpdateParseRequestStage.class).to(UpdateParseRequestStage.class);
        binder.bind(UpdateCreateResourceEntityStage.class).to(UpdateCreateResourceEntityStage.class);
        binder.bind(UpdateAuthorizeChangesStage.class).to(UpdateAuthorizeChangesStage.class);
        binder.bind(UpdateFilterResultStage.class).to(UpdateFilterResultStage.class);
        binder.bind(UpdateEncoderInstallStage.class).to(UpdateEncoderInstallStage.class);
        binder.bind(UpdateMergeChangesStage.class).to(UpdateMergeChangesStage.class);
        binder.bind(UpdateCommitStage.class).to(UpdateCommitStage.class);

        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.CREATE)).toInstance(UpdateFillResponseStage.getInstance());
        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.CREATE_OR_UPDATE)).toInstance(UpdateFillResponseStage.getInstance());
        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.IDEMPOTENT_CREATE_OR_UPDATE)).toInstance(UpdateFillResponseStage.getInstance());
        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.IDEMPOTENT_FULL_SYNC)).toInstance(UpdateFillResponseStage.getInstance());
        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.UPDATE)).toInstance(UpdateFillResponseStage.getInstance());

        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.CREATE)).toInstance(UpdateMapChangesStage.getInstance());
        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.CREATE_OR_UPDATE)).toInstance(UpdateMapChangesStage.getInstance());
        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.IDEMPOTENT_CREATE_OR_UPDATE)).toInstance(UpdateMapChangesStage.getInstance());
        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.IDEMPOTENT_FULL_SYNC)).toInstance(UpdateMapChangesStage.getInstance());
        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.UPDATE)).toInstance(UpdateMapChangesStage.getInstance());

        // delete stages
        binder.bind(DeleteProcessorFactory.class).toProvider(DeleteProcessorFactoryProvider.class);
        binder.bind(DeleteStartStage.class).to(DeleteStartStage.class);
        binder.bind(DeleteMapChangesStage.class).to(DeleteMapChangesStage.class);
        binder.bind(DeleteAuthorizeChangesStage.class).to(DeleteAuthorizeChangesStage.class);
        binder.bind(DeleteInDataStoreStage.class).to(DeleteInDataStoreStage.class);

        // unrelate stages
        binder.bind(UnrelateProcessorFactory.class).toProvider(UnrelateProcessorFactoryProvider.class);
        binder.bind(UnrelateStartStage.class).to(UnrelateStartStage.class);
        binder.bind(UnrelateUpdateDateStoreStage.class).to(UnrelateUpdateDateStoreStage.class);

        // a map of custom encoders
        binder.bindMap(Encoder.class);
        binder.bind(IEncodablePropertyFactory.class).to(EncodablePropertyFactory.class);
        binder.bind(ValueEncoders.class).toProvider(ValueEncodersProvider.class);

        // custom from JSON converters
        binder.bindMap(JsonValueConverter.class)
                .put(Object.class.getName(), GenericConverter.converter())
                .put("byte[]", Base64Converter.converter())
                .put(BigDecimal.class.getName(), BigDecimalConverter.converter())
                .put(Float.class.getName(), FloatConverter.converter())
                .put("float", FloatConverter.converter())
                .put(Double.class.getName(), DoubleConverter.converter())
                .put("double", DoubleConverter.converter())
                .put(Long.class.getName(), LongConverter.converter())
                .put("long", LongConverter.converter())
                .put(Date.class.getName(), UtcDateConverter.converter())
                .put(java.sql.Date.class.getName(), UtcDateConverter.converter(java.sql.Date.class))
                .put(java.sql.Time.class.getName(), UtcDateConverter.converter(java.sql.Time.class))
                .put(java.sql.Timestamp.class.getName(), UtcDateConverter.converter(java.sql.Timestamp.class))
                .put(LocalDate.class.getName(), io.agrest.converter.jsonvalue.ISOLocalDateConverter.converter())
                .put(LocalTime.class.getName(), io.agrest.converter.jsonvalue.ISOLocalTimeConverter.converter())
                .put(LocalDateTime.class.getName(), io.agrest.converter.jsonvalue.ISOLocalDateTimeConverter.converter())
                .put(OffsetDateTime.class.getName(), io.agrest.converter.jsonvalue.ISOOffsetDateTimeConverter.converter())
                .put(JsonNode.class.getName(), JsonNodeConverter.converter());

        binder.bind(JsonValueConverters.class).toProvider(JsonValueConvertersProvider.class);

        // custom to String converters
        binder.bindMap(ValueStringConverter.class)
                .put(LocalDate.class.getName(), LocalDateConverter.converter())
                .put(LocalTime.class.getName(), LocalTimeConverter.converter())
                .put(LocalDateTime.class.getName(), LocalDateTimeConverter.converter())
                .put(OffsetDateTime.class.getName(), OffsetDateTimeConverter.converter())
                .put(Date.class.getName(), UtilDateConverter.converter())
                .put(Timestamp.class.getName(), SqlTimestampConverter.converter())
                .put(java.sql.Date.class.getName(), SqlDateConverter.converter())
                .put(Time.class.getName(), SqlTimeConverter.converter());

        binder.bind(ValueStringConverters.class).toProvider(ValueStringConvertersProvider.class);

        binder.bind(EncoderFactory.class).to(EncoderFactory.class);
        binder.bind(IRelationshipMapper.class).to(RelationshipMapper.class);
        binder.bind(AgSchema.class).toProvider(LazySchemaProvider.class);
        binder.bind(IConstraintsHandler.class).to(ConstraintsHandler.class);

        binder.bind(IJacksonService.class).toProvider(JacksonServiceProvider.class);

        // Query parameter parsers from the UriInfo
        binder.bind(IExpParser.class).to(ExpParser.class);
        binder.bind(ISizeParser.class).to(SizeParser.class);
        binder.bind(ISortParser.class).to(SortParser.class);
        binder.bind(IExcludeParser.class).to(ExcludeParser.class);
        binder.bind(IIncludeParser.class).to(IncludeParser.class);

        binder.bind(IAgRequestBuilderFactory.class).to(DefaultRequestBuilderFactory.class);

        binder.bind(IExpMerger.class).to(ExpMerger.class);
        binder.bind(ISortMerger.class).to(SortMerger.class);
        binder.bind(IMapByMerger.class).to(MapByMerger.class);
        binder.bind(ISizeMerger.class).to(SizeMerger.class);
        binder.bind(IIncludeMerger.class).to(IncludeMerger.class);
        binder.bind(IExcludeMerger.class).to(ExcludeMerger.class);
        binder.bind(IResultFilter.class).to(ResultFilter.class);
        binder.bind(IChangeAuthorizer.class).to(ChangeAuthorizer.class);

        binder.bind(IEntityUpdateParser.class).to(EntityUpdateParser.class);
    }
}
