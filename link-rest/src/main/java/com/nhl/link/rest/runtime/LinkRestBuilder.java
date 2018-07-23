package com.nhl.link.rest.runtime;

import com.nhl.link.rest.BaseModule;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityConstraint;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.LrFeatureProvider;
import com.nhl.link.rest.LrModuleProvider;
import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.encoder.PropertyMetadataEncoder;
import com.nhl.link.rest.encoder.converter.StringConverter;
import com.nhl.link.rest.meta.LrEntityOverlay;
import com.nhl.link.rest.meta.cayenne.CayenneEntityCompiler;
import com.nhl.link.rest.meta.compiler.LrEntityCompiler;
import com.nhl.link.rest.meta.compiler.PojoEntityCompiler;
import com.nhl.link.rest.meta.parser.IResourceParser;
import com.nhl.link.rest.meta.parser.ResourceParser;
import com.nhl.link.rest.provider.CayenneRuntimeExceptionMapper;
import com.nhl.link.rest.provider.DataResponseWriter;
import com.nhl.link.rest.provider.LinkRestExceptionMapper;
import com.nhl.link.rest.provider.MetadataResponseWriter;
import com.nhl.link.rest.provider.SimpleResponseWriter;
import com.nhl.link.rest.provider.ValidationExceptionMapper;
import com.nhl.link.rest.runtime.adapter.LinkRestAdapter;
import com.nhl.link.rest.runtime.cayenne.CayennePersister;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.cayenne.NoCayennePersister;
import com.nhl.link.rest.runtime.cayenne.processor.delete.CayenneDeleteProcessorFactoryProvider;
import com.nhl.link.rest.runtime.cayenne.processor.delete.CayenneDeleteStage;
import com.nhl.link.rest.runtime.cayenne.processor.delete.CayenneDeleteStartStage;
import com.nhl.link.rest.runtime.cayenne.processor.select.CayenneAssembleQueryStage;
import com.nhl.link.rest.runtime.cayenne.processor.select.CayenneFetchDataStage;
import com.nhl.link.rest.runtime.cayenne.processor.select.CayenneSelectProcessorFactoryProvider;
import com.nhl.link.rest.runtime.cayenne.processor.unrelate.CayenneUnrelateDataStoreStage;
import com.nhl.link.rest.runtime.cayenne.processor.unrelate.CayenneUnrelateProcessorFactoryProvider;
import com.nhl.link.rest.runtime.cayenne.processor.unrelate.CayenneUnrelateStartStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneCreateOrUpdateStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneCreateStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneCreatedResponseStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneIdempotentCreateOrUpdateStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneIdempotentFullSyncStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneOkResponseStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneUpdateProcessorFactoryFactoryProvider;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneUpdateStage;
import com.nhl.link.rest.runtime.cayenne.processor.update.CayenneUpdateStartStage;
import com.nhl.link.rest.runtime.constraints.ConstraintsHandler;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.AttributeEncoderFactoryProvider;
import com.nhl.link.rest.runtime.encoder.EncoderService;
import com.nhl.link.rest.runtime.encoder.IAttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.encoder.IStringConverterFactory;
import com.nhl.link.rest.runtime.encoder.StringConverterFactoryProvider;
import com.nhl.link.rest.runtime.executor.UnboundedExecutorServiceProvider;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.meta.BaseUrlProvider;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.IResourceMetadataService;
import com.nhl.link.rest.runtime.meta.MetadataService;
import com.nhl.link.rest.runtime.meta.ResourceMetadataService;
import com.nhl.link.rest.runtime.parser.entityupdate.IEntityUpdateParser;
import com.nhl.link.rest.runtime.parser.entityupdate.EntityUpdateParser;
import com.nhl.link.rest.runtime.path.IPathDescriptorManager;
import com.nhl.link.rest.runtime.path.PathDescriptorManager;
import com.nhl.link.rest.runtime.parser.filter.CayenneExpParser;
import com.nhl.link.rest.runtime.parser.filter.CayenneExpConstructor;
import com.nhl.link.rest.runtime.parser.filter.ExpressionPostProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpConstructor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpParser;
import com.nhl.link.rest.runtime.parser.filter.IExpressionPostProcessor;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByConstructor;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByParser;
import com.nhl.link.rest.runtime.parser.mapBy.MapByConstructor;
import com.nhl.link.rest.runtime.parser.mapBy.MapByParser;
import com.nhl.link.rest.runtime.parser.size.ISizeConstructor;
import com.nhl.link.rest.runtime.parser.size.ISizeParser;
import com.nhl.link.rest.runtime.parser.size.SizeConstructor;
import com.nhl.link.rest.runtime.parser.size.SizeParser;
import com.nhl.link.rest.runtime.parser.sort.ISortParser;
import com.nhl.link.rest.runtime.parser.sort.ISortConstructor;
import com.nhl.link.rest.runtime.parser.sort.SortParser;
import com.nhl.link.rest.runtime.parser.sort.SortConstructor;
import com.nhl.link.rest.runtime.parser.tree.ExcludeParser;
import com.nhl.link.rest.runtime.parser.tree.ExcludeConstructor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeParser;
import com.nhl.link.rest.runtime.parser.tree.IExcludeConstructor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeParser;
import com.nhl.link.rest.runtime.parser.tree.IIncludeConstructor;
import com.nhl.link.rest.runtime.parser.tree.IncludeParser;
import com.nhl.link.rest.runtime.parser.tree.IncludeConstructor;
import com.nhl.link.rest.runtime.processor.delete.DeleteProcessorFactory;
import com.nhl.link.rest.runtime.processor.meta.CollectMetadataStage;
import com.nhl.link.rest.runtime.processor.meta.MetadataProcessorFactory;
import com.nhl.link.rest.runtime.processor.meta.MetadataProcessorFactoryProvider;
import com.nhl.link.rest.runtime.processor.select.ApplyServerParamsStage;
import com.nhl.link.rest.runtime.processor.select.ConstructResourceEntityStage;
import com.nhl.link.rest.runtime.processor.select.ParseRequestStage;
import com.nhl.link.rest.runtime.processor.select.SelectProcessorFactory;
import com.nhl.link.rest.runtime.processor.select.StartStage;
import com.nhl.link.rest.runtime.processor.unrelate.UnrelateProcessorFactory;
import com.nhl.link.rest.runtime.processor.update.UpdateProcessorFactoryFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;
import com.nhl.link.rest.runtime.shutdown.ShutdownManager;
import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
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
 * A builder of LinkRest runtime that can be loaded into JAX-RS 2 container as a
 * {@link Feature}.
 */
public class LinkRestBuilder {

    private ICayennePersister cayenneService;
    private Class<? extends ILinkRestService> linkRestServiceType;
    private ILinkRestService linkRestService;
    private List<LrModuleProvider> moduleProviders;
    private List<Module> modules;
    private List<LrFeatureProvider> featureProviders;
    private List<Feature> features;
    private List<EncoderFilter> encoderFilters;
    private Map<String, LrEntityOverlay> entityOverlays;
    private Map<Class<?>, Class<?>> exceptionMappers;
    private Collection<LinkRestAdapter> adapters;
    private Map<String, PropertyMetadataEncoder> metadataEncoders;
    private ExecutorService executor;
    private String baseUrl;
    private boolean autoLoadModules;
    private boolean autoLoadFeatures;

    /**
     * A shortcut that creates a LinkRest stack based on Cayenne runtime and
     * default settings.
     *
     * @since 1.14
     */
    public static LinkRestRuntime build(ServerRuntime cayenneRuntime) {
        return builder(cayenneRuntime).build();
    }

    /**
     * A shortcut that creates a LinkRestBuilder, setting its Cayenne runtime. A
     * caller can continue customizing the returned builder.
     *
     * @since 1.14
     */
    public static LinkRestBuilder builder(ServerRuntime cayenneRuntime) {
        return new LinkRestBuilder().cayenneRuntime(cayenneRuntime);
    }

    public LinkRestBuilder() {
        this.autoLoadModules = true;
        this.autoLoadFeatures = true;
        this.entityOverlays = new HashMap<>();
        this.encoderFilters = new ArrayList<>();
        this.linkRestServiceType = DefaultLinkRestService.class;
        this.cayenneService = NoCayennePersister.instance();
        this.exceptionMappers = mapDefaultExceptions();
        this.adapters = new ArrayList<>();
        this.metadataEncoders = new HashMap<>();
        this.moduleProviders = new ArrayList<>(5);
        this.modules = new ArrayList<>(5);
        this.featureProviders = new ArrayList<>(5);
        this.features = new ArrayList<>(5);
    }

    protected Map<Class<?>, Class<?>> mapDefaultExceptions() {

        Map<Class<?>, Class<?>> map = new HashMap<>();
        map.put(CayenneRuntimeException.class, CayenneRuntimeExceptionMapper.class);
        map.put(LinkRestException.class, LinkRestExceptionMapper.class);
        map.put(ValidationException.class, ValidationExceptionMapper.class);

        return map;
    }

    /**
     * Suppresses JAX-RS Feature auto-loading. By default features are auto-loaded based on the service descriptors under
     * "META-INF/services/com.nhl.link.rest.LrFeatureProvider". Calling this method would suppress auto-loading behavior,
     * letting the programmer explicitly pick which extensions need to be loaded.
     *
     * @return this builder instance.
     * @since 2.10
     */
    public LinkRestBuilder doNotAutoLoadFeatures() {
        this.autoLoadFeatures = false;
        return this;
    }

    /**
     * Suppresses module auto-loading. By default modules are auto-loaded based on the service descriptors under
     * "META-INF/services/com.nhl.link.rest.LrModuleProvider". Calling this method would suppress auto-loading behavior,
     * letting the programmer explicitly pick which extensions need to be loaded.
     *
     * @return this builder instance.
     * @since 2.10
     */
    public LinkRestBuilder doNotAutoLoadModules() {
        this.autoLoadModules = false;
        return this;
    }

    /**
     * Maps an ExceptionMapper for a given type of Exceptions. While this method
     * can be used for arbitrary exceptions, it is most useful to override the
     * default exception handlers defined in LinkRest for the following
     * exceptions: {@link LinkRestException}, {@link CayenneRuntimeException},
     * {@link ValidationException}.
     *
     * @since 1.1
     */
    public <E extends Throwable> LinkRestBuilder mapException(Class<? extends ExceptionMapper<E>> mapper) {

        for (Type t : mapper.getGenericInterfaces()) {

            if (t instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) t;
                if (ExceptionMapper.class.equals(pt.getRawType())) {
                    Type[] args = pt.getActualTypeArguments();
                    exceptionMappers.put((Class<?>) args[0], mapper);
                    return this;
                }
            }

        }

        throw new IllegalArgumentException("Failed to register ExceptionMapper: " + mapper.getName());
    }

    public LinkRestBuilder linkRestService(ILinkRestService linkRestService) {
        this.linkRestService = linkRestService;
        this.linkRestServiceType = null;
        return this;
    }

    public LinkRestBuilder linkRestService(Class<? extends ILinkRestService> linkRestServiceType) {
        this.linkRestService = null;
        this.linkRestServiceType = linkRestServiceType;
        return this;
    }

    public LinkRestBuilder cayenneRuntime(ServerRuntime cayenneRuntime) {
        this.cayenneService = new CayennePersister(cayenneRuntime);
        return this;
    }

    public LinkRestBuilder cayenneService(ICayennePersister cayenneService) {
        this.cayenneService = cayenneService;
        return this;
    }

    public LinkRestBuilder encoderFilter(EncoderFilter filter) {
        this.encoderFilters.add(filter);
        return this;
    }

    public LinkRestBuilder encoderFilters(Collection<EncoderFilter> filters) {
        this.encoderFilters.addAll(filters);
        return this;
    }

    /**
     * Sets the public base URL of the application serving this LinkRest stack. This should be a URL of the root REST
     * resource of the application. This value is used to build hypermedia controls (i.e. links) in the metadata
     * responses. It is optional, and for most apps can be calculated automatically. Usually has to be set explicitly
     * in case of a misconfigured reverse proxy (missing "X-Forwarded-Proto" header to tell apart HTTP from HTTPS), and
     * such.
     *
     * @param url a URL of the root REST resource of the application.
     * @return this builder instance
     * @since 2.10
     */
    public LinkRestBuilder baseUrl(String url) {
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
    public LinkRestBuilder executor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    /**
     * @since 1.12
     * @deprecated since 2.10. Instead use {@link LrEntityOverlay#addAttribute(String)}, and register
     * the overlay via {@link #entityOverlay(LrEntityOverlay)}.
     */
    @Deprecated
    public LinkRestBuilder transientProperty(Class<?> type, String propertyName) {
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
    public <T> LinkRestBuilder entityOverlay(LrEntityOverlay<T> overlay) {
        getOrCreateOverlay(overlay.getType()).merge(overlay);
        return this;
    }

    private <T> LrEntityOverlay<T> getOrCreateOverlay(Class<T> type) {
        return entityOverlays.computeIfAbsent(type.getName(), n -> new LrEntityOverlay<>(type));
    }

    /**
     * Adds an adapter that may contribute custom configuration to
     * {@link LinkRestRuntime}.
     *
     * @return this builder instance.
     * @since 1.3
     * @deprecated since 2.10 LinkRestAdapter is deprecated in favor of
     * {@link com.nhl.link.rest.LrFeatureProvider} and
     * {@link com.nhl.link.rest.LrModuleProvider}. Either can be registered with
     * {@link com.nhl.link.rest.runtime.LinkRestBuilder} explicitly or used to implemented auto-loadable extensions.
     */
    @Deprecated
    public LinkRestBuilder adapter(LinkRestAdapter adapter) {
        this.adapters.add(adapter);
        return this;
    }

    /**
     * Registers a JAX-RS feature extending LinkRest JAX-RS integration.
     *
     * @param feature a custom JAX-RS feature.
     * @return this builder instance.
     * @since 2.10
     */
    public LinkRestBuilder feature(Feature feature) {
        features.add(feature);
        return this;
    }

    /**
     * Registers a provider of a custom JAX-RS feature extending LinkRest JAX-RS integration.
     *
     * @param featureProvider a provider of a custom JAX-RS feature.
     * @return this builder instance.
     * @since 2.10
     */
    public LinkRestBuilder feature(LrFeatureProvider featureProvider) {
        featureProviders.add(featureProvider);
        return this;
    }

    /**
     * Registers a DI extension module for {@link LinkRestRuntime}.
     *
     * @param module an extension DI module for {@link LinkRestRuntime}.
     * @return this builder instance.
     * @since 2.10
     */
    public LinkRestBuilder module(Module module) {
        modules.add(module);
        return this;
    }

    /**
     * Registers a provider of a DI extension module for {@link LinkRestRuntime}.
     *
     * @param provider a provider of an extension module for {@link LinkRestRuntime}.
     * @return this builder instance.
     * @since 2.10
     */
    public LinkRestBuilder module(LrModuleProvider provider) {
        moduleProviders.add(provider);
        return this;
    }

    public LinkRestBuilder metadataEncoder(String type, PropertyMetadataEncoder encoder) {
        this.metadataEncoders.put(type, encoder);
        return this;
    }

    public LinkRestRuntime build() {
        Injector i = createInjector();
        return new LinkRestRuntime(i, createExtraFeatures(i), createExtraComponents());
    }

    private Collection<Class<?>> createExtraComponents() {
        // for now the only extra components are exception mappers
        return exceptionMappers.values();
    }

    private Collection<Feature> createExtraFeatures(Injector injector) {

        Collection<Feature> featureCollector = new ArrayList<>();

        if (!adapters.isEmpty()) {
            loadAdapterProvidedFeatures(featureCollector);
        }

        if (autoLoadFeatures) {
            loadAutoLoadableFeatures(featureCollector, injector);
        }

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
        ServiceLoader.load(LrFeatureProvider.class).forEach(fp -> collector.add(fp.feature(i)));
    }

    private void loadAdapterProvidedFeatures(Collection<Feature> collector) {
        adapters.forEach(a -> a.contributeToJaxRs(collector));
    }

    private void loadBuilderFeatures(Collection<Feature> collector, Injector i) {
        collector.addAll(this.features);
        featureProviders.forEach(fp -> collector.add(fp.feature(i)));
    }

    private void loadAutoLoadableModules(Collection<Module> collector) {
        collector.addAll(new ModuleLoader().load(LrModuleProvider.class));
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

        if (linkRestService == null && linkRestServiceType == null) {
            throw new IllegalStateException("Required 'linkRestService' is not set");
        }

        return binder -> {

            binder.bindList(EncoderFilter.class).addAll(encoderFilters);

            binder.bind(CayenneEntityCompiler.class).to(CayenneEntityCompiler.class);
            binder.bind(PojoEntityCompiler.class).to(PojoEntityCompiler.class);
            binder.bindList(LrEntityCompiler.class)
                    .add(CayenneEntityCompiler.class)
                    .add(PojoEntityCompiler.class);

            binder.bindMap(LrEntityOverlay.class).putAll(entityOverlays);
            binder.bindMap(Class.class, LinkRestRuntime.BODY_WRITERS_MAP)
                    .put(SimpleResponse.class.getName(), SimpleResponseWriter.class)
                    .put(DataResponse.class.getName(), DataResponseWriter.class)
                    .put(MetadataResponse.class.getName(), MetadataResponseWriter.class);

            binder.bindList(EntityConstraint.class, ConstraintsHandler.DEFAULT_READ_CONSTRAINTS_LIST);
            binder.bindList(EntityConstraint.class, ConstraintsHandler.DEFAULT_WRITE_CONSTRAINTS_LIST);
            binder.bindMap(PropertyMetadataEncoder.class).putAll(metadataEncoders);

            if (linkRestServiceType != null) {
                binder.bind(ILinkRestService.class).to(linkRestServiceType);
            } else {
                binder.bind(ILinkRestService.class).toInstance(linkRestService);
            }

            // select stages
            binder.bind(SelectProcessorFactory.class).toProvider(CayenneSelectProcessorFactoryProvider.class);
            binder.bind(StartStage.class).to(StartStage.class);
            binder.bind(ParseRequestStage.class).to(ParseRequestStage.class);
            binder.bind(ConstructResourceEntityStage.class).to(ConstructResourceEntityStage.class);
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
            binder.bind(com.nhl.link.rest.runtime.processor.update.ParseRequestStage.class)
                    .to(com.nhl.link.rest.runtime.processor.update.ParseRequestStage.class);
            binder.bind(com.nhl.link.rest.runtime.processor.update.ConstructResourceEntityStage.class)
                    .to(com.nhl.link.rest.runtime.processor.update.ConstructResourceEntityStage.class);
            binder.bind(com.nhl.link.rest.runtime.processor.update.ApplyServerParamsStage.class)
                    .to(com.nhl.link.rest.runtime.processor.update.ApplyServerParamsStage.class);
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

            // Constructors to create ResourceEntity from Query parameters
            binder.bind(ICayenneExpConstructor.class).to(CayenneExpConstructor.class);
            binder.bind(ISortConstructor.class).to(SortConstructor.class);
            binder.bind(IMapByConstructor.class).to(MapByConstructor.class);
            binder.bind(ISizeConstructor.class).to(SizeConstructor.class);
            binder.bind(IIncludeConstructor.class).to(IncludeConstructor.class);
            binder.bind(IExcludeConstructor.class).to(ExcludeConstructor.class);


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
