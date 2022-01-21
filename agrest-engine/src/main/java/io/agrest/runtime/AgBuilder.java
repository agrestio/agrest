package io.agrest.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.AgModuleProvider;
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
import io.agrest.converter.valuestring.ISODateConverter;
import io.agrest.converter.valuestring.ISODateTimeConverter;
import io.agrest.converter.valuestring.ISOLocalDateConverter;
import io.agrest.converter.valuestring.ISOLocalDateTimeConverter;
import io.agrest.converter.valuestring.ISOLocalTimeConverter;
import io.agrest.converter.valuestring.ISOOffsetDateTimeConverter;
import io.agrest.converter.valuestring.ISOTimeConverter;
import io.agrest.converter.valuestring.ValueStringConverter;
import io.agrest.converter.valuestring.ValueStringConverters;
import io.agrest.converter.valuestring.ValueStringConvertersProvider;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.PropertyMetadataEncoder;
import io.agrest.encoder.ValueEncoders;
import io.agrest.encoder.ValueEncodersProvider;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.parser.IResourceParser;
import io.agrest.meta.parser.ResourceParser;
import io.agrest.runtime.constraints.ConstraintsHandler;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.encoder.EncodablePropertyFactory;
import io.agrest.runtime.encoder.EncoderService;
import io.agrest.runtime.encoder.IEncodablePropertyFactory;
import io.agrest.runtime.encoder.IEncoderService;
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
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.runtime.meta.BaseUrlProvider;
import io.agrest.runtime.meta.IResourceMetadataService;
import io.agrest.runtime.meta.LazyAgDataMapProvider;
import io.agrest.runtime.meta.ResourceMetadataService;
import io.agrest.runtime.processor.meta.CollectMetadataStage;
import io.agrest.runtime.processor.meta.MetadataProcessorFactory;
import io.agrest.runtime.processor.meta.MetadataProcessorFactoryProvider;
import io.agrest.runtime.processor.select.ApplyServerParamsStage;
import io.agrest.runtime.processor.select.AssembleQueryStage;
import io.agrest.runtime.processor.select.CreateResourceEntityStage;
import io.agrest.runtime.processor.select.EncoderInstallStage;
import io.agrest.runtime.processor.select.FetchDataStage;
import io.agrest.runtime.processor.select.FilterResultStage;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import io.agrest.runtime.processor.select.SelectProcessorFactoryProvider;
import io.agrest.runtime.processor.select.StartStage;
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
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.MapBuilder;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.di.spi.ModuleLoader;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A builder of Agrest runtime.
 */
public class AgBuilder {

    private Class<? extends IAgService> agServiceType;
    private IAgService agService;
    private final List<AgModuleProvider> moduleProviders;
    private final List<Module> modules;

    private final Map<String, AgEntityOverlay> entityOverlays;
    private final Map<String, Class<? extends AgExceptionMapper>> exceptionMappers;

    @Deprecated
    private final Map<String, PropertyMetadataEncoder> metadataEncoders;
    @Deprecated
    private String baseUrl;

    private boolean autoLoadModules;

    public AgBuilder() {
        this.autoLoadModules = true;
        this.entityOverlays = new HashMap<>();
        this.agServiceType = DefaultAgService.class;
        this.exceptionMappers = new HashMap<>();
        this.metadataEncoders = new HashMap<>();
        this.moduleProviders = new ArrayList<>(5);
        this.modules = new ArrayList<>(5);
    }

    /**
     * Suppresses module auto-loading. By default modules are auto-loaded based on the service descriptors under
     * "META-INF/services/io.agrest.AgModuleProvider". Calling this method would suppress auto-loading behavior,
     * letting the programmer explicitly pick which extensions need to be loaded.
     *
     * @return this builder instance.
     * @since 2.10
     */
    public AgBuilder doNotAutoLoadModules() {
        this.autoLoadModules = false;
        return this;
    }

    public AgBuilder agService(IAgService agService) {
        this.agService = agService;
        this.agServiceType = null;
        return this;
    }

    public AgBuilder agService(Class<? extends IAgService> agServiceType) {
        this.agService = null;
        this.agServiceType = agServiceType;
        return this;
    }

    /**
     * Sets the public base URL of the application serving this Agrest stack. This should be a URL of the root REST
     * resource of the application. This value is used to build hypermedia controls (i.e. links) in the metadata
     * responses. It is optional, and for most apps can be calculated automatically. Usually has to be set explicitly
     * in case of a misconfigured reverse proxy (missing "X-Forwarded-Proto" header to tell apart HTTP from HTTPS), and
     * such.
     *
     * @param url a URL of the root REST resource of the application.
     * @return this builder instance
     * @since 2.10
     * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
     */
    // TODO: this may be useful for the future hypermedia controls (like pagination "next" links),
    //  but for now this is of no use
    @Deprecated
    public AgBuilder baseUrl(String url) {
        this.baseUrl = url;
        return this;
    }

    /**
     * Adds a descriptor of extra properties of a particular entity. If multiple overlays are registered for the
     * same entity, they are merged together. If they have overlapping properties, the last overlay wins.
     *
     * @see io.agrest.SelectBuilder#entityOverlay(AgEntityOverlay)
     * @since 2.10
     */
    public <T> AgBuilder entityOverlay(AgEntityOverlay<T> overlay) {
        getOrCreateOverlay(overlay.getType()).merge(overlay);
        return this;
    }

    private <T> AgEntityOverlay<T> getOrCreateOverlay(Class<T> type) {
        return entityOverlays.computeIfAbsent(type.getName(), n -> new AgEntityOverlay<>(type));
    }

    /**
     * Registers a DI extension module for {@link AgRuntime}.
     *
     * @param module an extension DI module for {@link AgRuntime}.
     * @return this builder instance.
     * @since 2.10
     */
    public AgBuilder module(Module module) {
        modules.add(module);
        return this;
    }

    /**
     * Registers a provider of a DI extension module for {@link AgRuntime}.
     *
     * @param provider a provider of an extension module for {@link AgRuntime}.
     * @return this builder instance.
     * @since 2.10
     */
    public AgBuilder module(AgModuleProvider provider) {
        moduleProviders.add(provider);
        return this;
    }

    /**
     * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
     */
    @Deprecated
    public AgBuilder metadataEncoder(String type, PropertyMetadataEncoder encoder) {
        this.metadataEncoders.put(type, encoder);
        return this;
    }

    public AgRuntime build() {
        return new AgRuntime(createInjector());
    }

    private Injector createInjector() {

        Collection<Module> moduleCollector = new ArrayList<>();

        // core module goes first, the rest of modules override the core and each other
        moduleCollector.add(createCoreModule());

        // TODO: consistent sorting policy past core module...
        // Cayenne ModuleProvider provides a sorting facility but how do we apply it across loading strategies ?

        if (autoLoadModules) {
            loadAutoLoadableModules(moduleCollector);
        }

        loadBuilderModules(moduleCollector);

        return DIBootstrap.createInjector(moduleCollector);
    }




    private void loadAutoLoadableModules(Collection<Module> collector) {
        collector.addAll(new ModuleLoader().load(AgModuleProvider.class));
    }

    private void loadBuilderModules(Collection<Module> collector) {

        // TODO: Pending a global sorting policy at the caller level, should we enforce builder addition order between
        // modules and providers?

        collector.addAll(modules);
        moduleProviders.forEach(p -> collector.add(p.module()));
    }

    private Module createCoreModule() {

        if (agService == null && agServiceType == null) {
            throw new IllegalStateException("Required 'agService' is not set");
        }

        return binder -> {

            binder.bind(AnnotationsAgEntityCompiler.class).to(AnnotationsAgEntityCompiler.class);
            binder.bindList(AgEntityCompiler.class).add(AnnotationsAgEntityCompiler.class);

            binder.bindMap(AgEntityOverlay.class).putAll(entityOverlays);



            if (agServiceType != null) {
                binder.bind(IAgService.class).to(agServiceType);
            } else {
                binder.bind(IAgService.class).toInstance(agService);
            }

            MapBuilder<AgExceptionMapper> mapperBuilder = binder
                    .bindMap(AgExceptionMapper.class)
                    .put(AgException.class.getName(), AgExceptionDefaultMapper.class);
            exceptionMappers.forEach(mapperBuilder::put);

            binder.bind(AgExceptionMappers.class).to(AgExceptionMappers.class);

            // select stages
            binder.bind(SelectProcessorFactory.class).toProvider(SelectProcessorFactoryProvider.class);
            binder.bind(StartStage.class).to(StartStage.class);
            binder.bind(CreateResourceEntityStage.class).to(CreateResourceEntityStage.class);
            binder.bind(ApplyServerParamsStage.class).to(ApplyServerParamsStage.class);
            binder.bind(EncoderInstallStage.class).to(EncoderInstallStage.class);
            binder.bind(AssembleQueryStage.class).to(AssembleQueryStage.class);
            binder.bind(FetchDataStage.class).to(FetchDataStage.class);
            binder.bind(FilterResultStage.class).to(FilterResultStage.class);

            // update stages
            binder.bind(io.agrest.runtime.processor.update.ParseRequestStage.class)
                    .to(io.agrest.runtime.processor.update.ParseRequestStage.class);
            binder.bind(io.agrest.runtime.processor.update.CreateResourceEntityStage.class)
                    .to(io.agrest.runtime.processor.update.CreateResourceEntityStage.class);
            binder.bind(io.agrest.runtime.processor.update.AuthorizeChangesStage.class)
                    .to(io.agrest.runtime.processor.update.AuthorizeChangesStage.class);
            binder.bind(io.agrest.runtime.processor.update.FilterResultStage.class)
                    .to(io.agrest.runtime.processor.update.FilterResultStage.class);
            binder.bind(io.agrest.runtime.processor.update.EncoderInstallStage.class)
                    .to(io.agrest.runtime.processor.update.EncoderInstallStage.class);

            // delete stages
            binder.bind(io.agrest.runtime.processor.delete.AuthorizeChangesStage.class)
                    .to(io.agrest.runtime.processor.delete.AuthorizeChangesStage.class);

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
                    .put(LocalDate.class.getName(), ISOLocalDateConverter.converter())
                    .put(LocalTime.class.getName(), ISOLocalTimeConverter.converter())
                    .put(LocalDateTime.class.getName(), ISOLocalDateTimeConverter.converter())
                    .put(OffsetDateTime.class.getName(), ISOOffsetDateTimeConverter.converter())
                    .put(java.util.Date.class.getName(), ISODateTimeConverter.converter())
                    .put(Timestamp.class.getName(), ISODateTimeConverter.converter())
                    .put(java.sql.Date.class.getName(), ISODateConverter.converter())
                    .put(Time.class.getName(), ISOTimeConverter.converter());

            binder.bind(ValueStringConverters.class).toProvider(ValueStringConvertersProvider.class);

            binder.bind(IEncoderService.class).to(EncoderService.class);
            binder.bind(IRelationshipMapper.class).to(RelationshipMapper.class);
            binder.bind(AgDataMap.class).toProvider(LazyAgDataMapProvider.class);
            binder.bind(IConstraintsHandler.class).to(ConstraintsHandler.class);

            binder.bind(IJacksonService.class).to(JacksonService.class);

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

            // deprecated services
            binder.bindMap(PropertyMetadataEncoder.class).putAll(metadataEncoders);
            binder.bind(MetadataProcessorFactory.class).toProvider(MetadataProcessorFactoryProvider.class);
            binder.bind(CollectMetadataStage.class).to(CollectMetadataStage.class);
            binder.bind(IResourceMetadataService.class).to(ResourceMetadataService.class);
            binder.bind(IResourceParser.class).to(ResourceParser.class);
            binder.bind(BaseUrlProvider.class).toInstance(BaseUrlProvider.forUrl(Optional.ofNullable(baseUrl)));
        };
    }
}
