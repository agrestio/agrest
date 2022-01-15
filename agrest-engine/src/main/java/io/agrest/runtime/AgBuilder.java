package io.agrest.runtime;

import io.agrest.AgException;
import io.agrest.AgFeatureProvider;
import io.agrest.AgModuleProvider;
import io.agrest.DataResponse;
import io.agrest.MetadataResponse;
import io.agrest.SimpleResponse;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.converter.jsonvalue.DefaultJsonValueConverterFactoryProvider;
import io.agrest.converter.jsonvalue.IJsonValueConverterFactory;
import io.agrest.converter.jsonvalue.JsonValueConverter;
import io.agrest.converter.valuejson.ValueJsonConverter;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.PropertyMetadataEncoder;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.parser.IResourceParser;
import io.agrest.meta.parser.ResourceParser;
import io.agrest.provider.DataResponseWriter;
import io.agrest.provider.JaxrsAgExceptionMapper;
import io.agrest.provider.MetadataResponseWriter;
import io.agrest.provider.SimpleResponseWriter;
import io.agrest.runtime.constraints.ConstraintsHandler;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.encoder.EncodablePropertyFactory;
import io.agrest.runtime.encoder.EncoderService;
import io.agrest.runtime.encoder.IEncodablePropertyFactory;
import io.agrest.runtime.encoder.IEncoderService;
import io.agrest.runtime.encoder.IStringConverterFactory;
import io.agrest.runtime.encoder.StringConverterFactoryProvider;
import io.agrest.runtime.encoder.ValueEncoders;
import io.agrest.runtime.encoder.ValueEncodersProvider;
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
import io.agrest.runtime.processor.select.ParseRequestStage;
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

import javax.ws.rs.core.Feature;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * A builder of Agrest runtime that can be loaded into JAX-RS 2 container as a {@link Feature}.
 */
public class AgBuilder {

    private Class<? extends IAgService> agServiceType;
    private IAgService agService;
    private final List<AgModuleProvider> moduleProviders;
    private final List<Module> modules;
    private final List<AgFeatureProvider> featureProviders;
    private final List<Feature> features;
    private final Map<String, AgEntityOverlay> entityOverlays;
    private final Map<String, Class<? extends AgExceptionMapper>> exceptionMappers;

    @Deprecated
    private final Map<String, PropertyMetadataEncoder> metadataEncoders;
    private String baseUrl;
    private boolean autoLoadModules;
    private boolean autoLoadFeatures;

    public AgBuilder() {
        this.autoLoadModules = true;
        this.autoLoadFeatures = true;
        this.entityOverlays = new HashMap<>();
        this.agServiceType = DefaultAgService.class;
        this.exceptionMappers = new HashMap<>();
        this.metadataEncoders = new HashMap<>();
        this.moduleProviders = new ArrayList<>(5);
        this.modules = new ArrayList<>(5);
        this.featureProviders = new ArrayList<>(5);
        this.features = new ArrayList<>(5);
    }

    /**
     * Suppresses JAX-RS Feature auto-loading. By default features are auto-loaded based on the service descriptors under
     * "META-INF/services/io.agrest.AgFeatureProvider". Calling this method would suppress auto-loading behavior,
     * letting the programmer explicitly pick which extensions need to be loaded.
     *
     * @return this builder instance.
     * @since 2.10
     */
    public AgBuilder doNotAutoLoadFeatures() {
        this.autoLoadFeatures = false;
        return this;
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
     * Registers a JAX-RS feature extending Agrest JAX-RS integration.
     *
     * @param feature a custom JAX-RS feature.
     * @return this builder instance.
     * @since 2.10
     */
    public AgBuilder feature(Feature feature) {
        features.add(feature);
        return this;
    }

    /**
     * Registers a provider of a custom JAX-RS feature extending Agrest JAX-RS integration.
     *
     * @param featureProvider a provider of a custom JAX-RS feature.
     * @return this builder instance.
     * @since 2.10
     */
    public AgBuilder feature(AgFeatureProvider featureProvider) {
        featureProviders.add(featureProvider);
        return this;
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
        Injector i = createInjector();
        return new AgRuntime(i, createExtraFeatures(i));
    }

    private Collection<Feature> createExtraFeatures(Injector injector) {

        Collection<Feature> featureCollector = new ArrayList<>();

        if (autoLoadFeatures) {
            loadAutoLoadableFeatures(featureCollector, injector);
        }

        loadExceptionMapperFeature(featureCollector, injector);

        loadBuilderFeatures(featureCollector, injector);

        return featureCollector;
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

    private void loadAutoLoadableFeatures(Collection<Feature> collector, Injector i) {
        ServiceLoader.load(AgFeatureProvider.class).forEach(fp -> collector.add(fp.feature(i)));
    }

    private void loadExceptionMapperFeature(Collection<Feature> collector, Injector i) {

        collector.add(c -> {
            c.register(new JaxrsAgExceptionMapper());
            return true;
        });
    }

    private void loadBuilderFeatures(Collection<Feature> collector, Injector i) {
        collector.addAll(this.features);
        featureProviders.forEach(fp -> collector.add(fp.feature(i)));
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

            binder.bindMap(JsonValueConverter.class);
            binder.bind(IJsonValueConverterFactory.class).toProvider(DefaultJsonValueConverterFactoryProvider.class);

            binder.bind(AnnotationsAgEntityCompiler.class).to(AnnotationsAgEntityCompiler.class);
            binder.bindList(AgEntityCompiler.class).add(AnnotationsAgEntityCompiler.class);

            binder.bindMap(AgEntityOverlay.class).putAll(entityOverlays);
            binder.bindMap(Class.class, AgRuntime.BODY_WRITERS_MAP)
                    .put(SimpleResponse.class.getName(), SimpleResponseWriter.class)
                    .put(DataResponse.class.getName(), DataResponseWriter.class)
                    .put(MetadataResponse.class.getName(), MetadataResponseWriter.class);

            binder.bindMap(PropertyMetadataEncoder.class).putAll(metadataEncoders);

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
            binder.bind(ParseRequestStage.class).to(ParseRequestStage.class);
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

            // metadata stages
            binder.bind(MetadataProcessorFactory.class).toProvider(MetadataProcessorFactoryProvider.class);
            binder.bind(CollectMetadataStage.class).to(CollectMetadataStage.class);

            // a map of custom encoders
            binder.bindMap(Encoder.class);
            binder.bind(IEncodablePropertyFactory.class).to(EncodablePropertyFactory.class);
            binder.bind(ValueEncoders.class).toProvider(ValueEncodersProvider.class);

            // a map of custom converters
            binder.bindMap(ValueJsonConverter.class);
            binder.bind(IStringConverterFactory.class).toProvider(StringConverterFactoryProvider.class);

            binder.bind(IEncoderService.class).to(EncoderService.class);
            binder.bind(IRelationshipMapper.class).to(RelationshipMapper.class);
            binder.bind(AgDataMap.class).toProvider(LazyAgDataMapProvider.class);
            binder.bind(IResourceMetadataService.class).to(ResourceMetadataService.class);
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

            binder.bind(IResourceParser.class).to(ResourceParser.class);
            binder.bind(IEntityUpdateParser.class).to(EntityUpdateParser.class);

            Optional<String> maybeBaseUrl = Optional.ofNullable(baseUrl);
            binder.bind(BaseUrlProvider.class).toInstance(BaseUrlProvider.forUrl(maybeBaseUrl));
        };
    }
}
