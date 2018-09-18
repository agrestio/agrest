package io.agrest.runtime;

import io.agrest.AgFeatureProvider;
import io.agrest.AgModuleProvider;
import io.agrest.AgException;
import io.agrest.BaseModule;
import io.agrest.DataResponse;
import io.agrest.EntityConstraint;
import io.agrest.MetadataResponse;
import io.agrest.SimpleResponse;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncoderFilter;
import io.agrest.encoder.PropertyMetadataEncoder;
import io.agrest.encoder.converter.StringConverter;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.cayenne.CayenneEntityCompiler;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.meta.parser.IResourceParser;
import io.agrest.meta.parser.ResourceParser;
import io.agrest.provider.CayenneRuntimeExceptionMapper;
import io.agrest.provider.DataResponseWriter;
import io.agrest.provider.AgExceptionMapper;
import io.agrest.provider.MetadataResponseWriter;
import io.agrest.provider.SimpleResponseWriter;
import io.agrest.provider.ValidationExceptionMapper;
import io.agrest.runtime.adapter.AgAdapter;
import io.agrest.runtime.cayenne.CayennePersister;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.cayenne.NoCayennePersister;
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
import io.agrest.runtime.constraints.ConstraintsHandler;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.encoder.AttributeEncoderFactoryProvider;
import io.agrest.runtime.encoder.EncoderService;
import io.agrest.runtime.encoder.IAttributeEncoderFactory;
import io.agrest.runtime.encoder.IEncoderService;
import io.agrest.runtime.encoder.IStringConverterFactory;
import io.agrest.runtime.encoder.StringConverterFactoryProvider;
import io.agrest.runtime.entity.CayenneExpMerger;
import io.agrest.runtime.entity.ExcludeMerger;
import io.agrest.runtime.entity.ExpressionPostProcessor;
import io.agrest.runtime.entity.ICayenneExpMerger;
import io.agrest.runtime.entity.IExcludeMerger;
import io.agrest.runtime.entity.IExpressionPostProcessor;
import io.agrest.runtime.entity.IIncludeMerger;
import io.agrest.runtime.entity.IMapByMerger;
import io.agrest.runtime.entity.ISizeMerger;
import io.agrest.runtime.entity.ISortMerger;
import io.agrest.runtime.entity.IncludeMerger;
import io.agrest.runtime.entity.MapByMerger;
import io.agrest.runtime.entity.SizeMerger;
import io.agrest.runtime.entity.SortMerger;
import io.agrest.runtime.executor.UnboundedExecutorServiceProvider;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.runtime.meta.BaseUrlProvider;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.meta.IResourceMetadataService;
import io.agrest.runtime.meta.MetadataService;
import io.agrest.runtime.meta.ResourceMetadataService;
import io.agrest.runtime.path.IPathDescriptorManager;
import io.agrest.runtime.path.PathDescriptorManager;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.meta.CollectMetadataStage;
import io.agrest.runtime.processor.meta.MetadataProcessorFactory;
import io.agrest.runtime.processor.meta.MetadataProcessorFactoryProvider;
import io.agrest.runtime.processor.select.ApplyServerParamsStage;
import io.agrest.runtime.processor.select.CreateResourceEntityStage;
import io.agrest.runtime.processor.select.ParseRequestStage;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import io.agrest.runtime.processor.select.StartStage;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;
import io.agrest.runtime.processor.update.UpdateProcessorFactoryFactory;
import io.agrest.runtime.protocol.CayenneExpParser;
import io.agrest.runtime.protocol.EntityUpdateParser;
import io.agrest.runtime.protocol.ExcludeParser;
import io.agrest.runtime.protocol.ICayenneExpParser;
import io.agrest.runtime.protocol.IEntityUpdateParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.IMapByParser;
import io.agrest.runtime.protocol.ISizeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.protocol.IncludeParser;
import io.agrest.runtime.protocol.MapByParser;
import io.agrest.runtime.protocol.SizeParser;
import io.agrest.runtime.protocol.SortParser;
import io.agrest.runtime.provider.CayenneExpProvider;
import io.agrest.runtime.provider.ExcludeProvider;
import io.agrest.runtime.provider.IncludeProvider;
import io.agrest.runtime.provider.MapByProvider;
import io.agrest.runtime.provider.SizeProvider;
import io.agrest.runtime.provider.SortProvider;
import io.agrest.runtime.semantics.IRelationshipMapper;
import io.agrest.runtime.semantics.RelationshipMapper;
import io.agrest.runtime.shutdown.ShutdownManager;
import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Key;
import org.apache.cayenne.di.MapBuilder;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.di.spi.ModuleLoader;
import org.apache.cayenne.reflect.Accessor;
import org.apache.cayenne.reflect.PropertyUtils;
import org.apache.cayenne.validation.ValidationException;

import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.ExceptionMapper;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;

/**
 * A builder of AgREST runtime that can be loaded into JAX-RS 2 container as a {@link Feature}.
 */
public class AgBuilder {

    private ICayennePersister cayenneService;
    private Class<? extends IAgService> agServiceType;
    private IAgService agService;
    private List<AgModuleProvider> moduleProviders;
    private List<Module> modules;
    private List<AgFeatureProvider> featureProviders;
    private List<Feature> features;
    private List<EncoderFilter> encoderFilters;
    private Map<String, AgEntityOverlay> entityOverlays;
    private Map<String, Class<? extends ExceptionMapper>> exceptionMappers;
    private Collection<AgAdapter> adapters;
    private Map<String, PropertyMetadataEncoder> metadataEncoders;
    private ExecutorService executor;
    private String baseUrl;
    private boolean autoLoadModules;
    private boolean autoLoadFeatures;

    public AgBuilder() {
        this.autoLoadModules = true;
        this.autoLoadFeatures = true;
        this.entityOverlays = new HashMap<>();
        this.encoderFilters = new ArrayList<>();
        this.agServiceType = DefaultAgService.class;
        this.cayenneService = NoCayennePersister.instance();
        this.exceptionMappers = new HashMap<>();
        this.adapters = new ArrayList<>();
        this.metadataEncoders = new HashMap<>();
        this.moduleProviders = new ArrayList<>(5);
        this.modules = new ArrayList<>(5);
        this.featureProviders = new ArrayList<>(5);
        this.features = new ArrayList<>(5);
    }

    /**
     * A shortcut that creates a AgREST stack based on Cayenne runtime and
     * default settings.
     *
     * @since 1.14
     */
    public static AgRuntime build(ServerRuntime cayenneRuntime) {
        return builder(cayenneRuntime).build();
    }

    /**
     * A shortcut that creates a AgBuilder, setting its Cayenne runtime. A
     * caller can continue customizing the returned builder.
     *
     * @since 1.14
     */
    public static AgBuilder builder(ServerRuntime cayenneRuntime) {
        return new AgBuilder().cayenneRuntime(cayenneRuntime);
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

    /**
     * Maps an ExceptionMapper for a given type of Exceptions. While this method
     * can be used for arbitrary exceptions, it is most useful to override the
     * default exception handlers defined in AgREST for the following
     * exceptions: {@link AgException}, {@link CayenneRuntimeException},
     * {@link ValidationException}.
     *
     * @since 1.1
     * @deprecated since 2.13. Custom exception handlers can be added via a custom module or module provider. E.g.
     * <code>b.bindMap(ExceptionMapper.class).put(AgException.class.getName(), MyExceptionMapper.class)</code>
     */
    @Deprecated
    public <E extends Throwable> AgBuilder mapException(Class<? extends ExceptionMapper<E>> mapper) {

        for (Type t : mapper.getGenericInterfaces()) {

            if (t instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) t;
                if (ExceptionMapper.class.equals(pt.getRawType())) {
                    Type[] args = pt.getActualTypeArguments();
                    exceptionMappers.put(args[0].getTypeName(), mapper);
                    return this;
                }
            }
        }

        throw new IllegalArgumentException("Failed to register ExceptionMapper: " + mapper.getName());
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

    public AgBuilder cayenneRuntime(ServerRuntime cayenneRuntime) {
        this.cayenneService = new CayennePersister(cayenneRuntime);
        return this;
    }

    public AgBuilder cayenneService(ICayennePersister cayenneService) {
        this.cayenneService = cayenneService;
        return this;
    }

    public AgBuilder encoderFilter(EncoderFilter filter) {
        this.encoderFilters.add(filter);
        return this;
    }

    public AgBuilder encoderFilters(Collection<EncoderFilter> filters) {
        this.encoderFilters.addAll(filters);
        return this;
    }

    /**
     * Sets the public base URL of the application serving this AgREST stack. This should be a URL of the root REST
     * resource of the application. This value is used to build hypermedia controls (i.e. links) in the metadata
     * responses. It is optional, and for most apps can be calculated automatically. Usually has to be set explicitly
     * in case of a misconfigured reverse proxy (missing "X-Forwarded-Proto" header to tell apart HTTP from HTTPS), and
     * such.
     *
     * @param url a URL of the root REST resource of the application.
     * @return this builder instance
     * @since 2.10
     */
    public AgBuilder baseUrl(String url) {
        this.baseUrl = url;
        return this;
    }

    /**
     * Sets an optional thread pool that should be used by non-blocking request runners.
     *
     * @param executor a thread pool used for non-blocking request runners.
     * @return this builder instance.
     * @since 2.0
     */
    public AgBuilder executor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    /**
     * @since 1.12
     * @deprecated since 2.10. Instead use {@link AgEntityOverlay#addAttribute(String)}, and register
     * the overlay via {@link #entityOverlay(AgEntityOverlay)}.
     */
    @Deprecated
    public AgBuilder transientProperty(Class<?> type, String propertyName) {
        Accessor accessor = PropertyUtils.accessor(propertyName);
        getOrCreateOverlay(type).addAttribute(propertyName, Object.class, accessor::getValue);
        return this;
    }

    /**
     * Adds a descriptor of extra properties of a particular entity. If multiple overlays are registered for the
     * same entity, they are merged together. If they have overlapping properties, the last overlay wins.
     *
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
     * Adds an adapter that may contribute custom configuration to
     * {@link AgRuntime}.
     *
     * @return this builder instance.
     * @since 1.3
     * @deprecated since 2.10 AgAdapter is deprecated in favor of
     * {@link AgFeatureProvider} and
     * {@link AgModuleProvider}. Either can be registered with
     * {@link AgBuilder} explicitly or used to implemented auto-loadable extensions.
     */
    @Deprecated
    public AgBuilder adapter(AgAdapter adapter) {
        this.adapters.add(adapter);
        return this;
    }

    /**
     * Registers a JAX-RS feature extending AgREST JAX-RS integration.
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
     * Registers a provider of a custom JAX-RS feature extending AgREST JAX-RS integration.
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

        if (!adapters.isEmpty()) {
            loadAdapterProvidedFeatures(featureCollector);
        }

        if (autoLoadFeatures) {
            loadAutoLoadableFeatures(featureCollector, injector);
        }

        loadExceptionMapperFeature(featureCollector, injector);

        loadBuilderFeatures(featureCollector, injector);

        return featureCollector;
    }

    private Injector createInjector() {

        Collection<Module> moduleCollector = new ArrayList<>();

        // base and core module goes first, the rest of them override the core and each other
        moduleCollector.add(createBaseModule());
        moduleCollector.add(createCoreModule());

        // TODO: consistent sorting policy past core module...
        // Cayenne ModuleProvider provides a sorting facility but how do we apply it across loading strategies ?

        if (!adapters.isEmpty()) {
            loadAdapterProvidedModules(moduleCollector);
        }

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

        i.getInstance(Key.getMapOf(String.class, ExceptionMapper.class))
                .values()
                .forEach(em -> collector.add(c -> {
                    c.register(em);
                    return true;
                }));
    }

    private void loadAdapterProvidedFeatures(Collection<Feature> collector) {
        adapters.forEach(a -> a.contributeToJaxRs(collector));
    }

    private void loadBuilderFeatures(Collection<Feature> collector, Injector i) {
        collector.addAll(this.features);
        featureProviders.forEach(fp -> collector.add(fp.feature(i)));
    }

    private void loadAutoLoadableModules(Collection<Module> collector) {
        collector.addAll(new ModuleLoader().load(AgModuleProvider.class));
    }

    private void loadAdapterProvidedModules(Collection<Module> collector) {
        collector.add(b -> adapters.forEach(a -> a.contributeToRuntime(b)));
    }

    private void loadBuilderModules(Collection<Module> collector) {

        // TODO: Pending a global sorting policy at the caller level, should we enforce builder addition order between
        // modules and providers?

        collector.addAll(modules);
        moduleProviders.forEach(p -> collector.add(p.module()));
    }

    private Module createBaseModule() {
        return new BaseModule();
    }

    private Module createCoreModule() {

        if (agService == null && agServiceType == null) {
            throw new IllegalStateException("Required 'agService' is not set");
        }

        return binder -> {

            binder.bindList(EncoderFilter.class).addAll(encoderFilters);

            binder.bind(CayenneEntityCompiler.class).to(CayenneEntityCompiler.class);
            binder.bind(PojoEntityCompiler.class).to(PojoEntityCompiler.class);
            binder.bindList(AgEntityCompiler.class)
                    .add(CayenneEntityCompiler.class)
                    .add(PojoEntityCompiler.class);

            binder.bindMap(AgEntityOverlay.class).putAll(entityOverlays);
            binder.bindMap(Class.class, AgRuntime.BODY_WRITERS_MAP)
                    .put(SimpleResponse.class.getName(), SimpleResponseWriter.class)
                    .put(DataResponse.class.getName(), DataResponseWriter.class)
                    .put(MetadataResponse.class.getName(), MetadataResponseWriter.class);

            binder.bindList(EntityConstraint.class, ConstraintsHandler.DEFAULT_READ_CONSTRAINTS_LIST);
            binder.bindList(EntityConstraint.class, ConstraintsHandler.DEFAULT_WRITE_CONSTRAINTS_LIST);
            binder.bindMap(PropertyMetadataEncoder.class).putAll(metadataEncoders);

            if (agServiceType != null) {
                binder.bind(IAgService.class).to(agServiceType);
            } else {
                binder.bind(IAgService.class).toInstance(agService);
            }

            MapBuilder<ExceptionMapper> mapperBuilder = binder.bindMap(ExceptionMapper.class)
                    .put(CayenneRuntimeException.class.getName(), CayenneRuntimeExceptionMapper.class)
                    .put(AgException.class.getName(), AgExceptionMapper.class)
                    .put(ValidationException.class.getName(), ValidationExceptionMapper.class);

            // override with custom mappers
            exceptionMappers.forEach((n, m) -> mapperBuilder.put(n, m));

            // select stages
            binder.bind(SelectProcessorFactory.class).toProvider(CayenneSelectProcessorFactoryProvider.class);
            binder.bind(StartStage.class).to(StartStage.class);
            binder.bind(ParseRequestStage.class).to(ParseRequestStage.class);
            binder.bind(CreateResourceEntityStage.class).to(CreateResourceEntityStage.class);
            binder.bind(ApplyServerParamsStage.class).to(ApplyServerParamsStage.class);
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
            binder.bind(io.agrest.runtime.processor.update.ParseRequestStage.class)
                    .to(io.agrest.runtime.processor.update.ParseRequestStage.class);
            binder.bind(io.agrest.runtime.processor.update.CreateResourceEntityStage.class)
                    .to(io.agrest.runtime.processor.update.CreateResourceEntityStage.class);
            binder.bind(io.agrest.runtime.processor.update.ApplyServerParamsStage.class)
                    .to(io.agrest.runtime.processor.update.ApplyServerParamsStage.class);
            binder.bind(CayenneCreateStage.class).to(CayenneCreateStage.class);
            binder.bind(CayenneUpdateStage.class).to(CayenneUpdateStage.class);
            binder.bind(CayenneCreateOrUpdateStage.class).to(CayenneCreateOrUpdateStage.class);
            binder.bind(CayenneIdempotentCreateOrUpdateStage.class).to(CayenneIdempotentCreateOrUpdateStage.class);
            binder.bind(CayenneIdempotentFullSyncStage.class).to(CayenneIdempotentFullSyncStage.class);
            binder.bind(CayenneOkResponseStage.class).to(CayenneOkResponseStage.class);
            binder.bind(CayenneCreatedResponseStage.class).to(CayenneCreatedResponseStage.class);

            // metadata stages
            binder.bind(MetadataProcessorFactory.class).toProvider(MetadataProcessorFactoryProvider.class);
            binder.bind(CollectMetadataStage.class).to(CollectMetadataStage.class);

            // unrelate stages
            binder.bind(UnrelateProcessorFactory.class).toProvider(CayenneUnrelateProcessorFactoryProvider.class);
            binder.bind(CayenneUnrelateStartStage.class).to(CayenneUnrelateStartStage.class);
            binder.bind(CayenneUnrelateDataStoreStage.class).to(CayenneUnrelateDataStoreStage.class);

            // a map of custom encoders
            binder.bindMap(Encoder.class);
            binder.bind(IAttributeEncoderFactory.class).toProvider(AttributeEncoderFactoryProvider.class);

            // a map of custom converters
            binder.bindMap(StringConverter.class);
            binder.bind(IStringConverterFactory.class).toProvider(StringConverterFactoryProvider.class);

            binder.bind(IEncoderService.class).to(EncoderService.class);
            binder.bind(IRelationshipMapper.class).to(RelationshipMapper.class);
            binder.bind(IMetadataService.class).to(MetadataService.class);
            binder.bind(IResourceMetadataService.class).to(ResourceMetadataService.class);
            binder.bind(IConstraintsHandler.class).to(ConstraintsHandler.class);
            binder.bind(IExpressionPostProcessor.class).to(ExpressionPostProcessor.class);

            binder.bind(IJacksonService.class).to(JacksonService.class);
            binder.bind(ICayennePersister.class).toInstance(cayenneService);

            binder.bind(IPathDescriptorManager.class).to(PathDescriptorManager.class);

            // Query parameter parsers from the UriInfo
            binder.bind(ICayenneExpParser.class).to(CayenneExpParser.class);
            binder.bind(IMapByParser.class).to(MapByParser.class);
            binder.bind(ISizeParser.class).to(SizeParser.class);
            binder.bind(ISortParser.class).to(SortParser.class);
            binder.bind(IExcludeParser.class).to(ExcludeParser.class);
            binder.bind(IIncludeParser.class).to(IncludeParser.class);

            // Converter providers to get value objects from explicit query parameters
            binder.bind(CayenneExpProvider.class).to(CayenneExpProvider.class);
            binder.bind(IncludeProvider.class).to(IncludeProvider.class);
            binder.bind(ExcludeProvider.class).to(ExcludeProvider.class);
            binder.bind(SortProvider.class).to(SortProvider.class);
            binder.bind(MapByProvider.class).to(MapByProvider.class);
            binder.bind(SizeProvider.class).to(SizeProvider.class);

            // Constructors to create ResourceEntity from Query parameters
            binder.bind(ICayenneExpMerger.class).to(CayenneExpMerger.class);
            binder.bind(ISortMerger.class).to(SortMerger.class);
            binder.bind(IMapByMerger.class).to(MapByMerger.class);
            binder.bind(ISizeMerger.class).to(SizeMerger.class);
            binder.bind(IIncludeMerger.class).to(IncludeMerger.class);
            binder.bind(IExcludeMerger.class).to(ExcludeMerger.class);

            binder.bind(IResourceParser.class).to(ResourceParser.class);
            binder.bind(IEntityUpdateParser.class).to(EntityUpdateParser.class);

            Optional<String> maybeBaseUrl = Optional.ofNullable(baseUrl);
            binder.bind(BaseUrlProvider.class).toInstance(BaseUrlProvider.forUrl(maybeBaseUrl));

            binder.bind(ShutdownManager.class).toInstance(new ShutdownManager(Duration.ofSeconds(10)));

            if (executor != null) {
                binder.bind(ExecutorService.class).toInstance(executor);
            } else {
                binder.bind(ExecutorService.class).toProvider(UnboundedExecutorServiceProvider.class);
            }
        };
    }
}
