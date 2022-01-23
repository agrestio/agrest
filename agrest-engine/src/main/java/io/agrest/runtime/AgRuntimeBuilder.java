package io.agrest.runtime;

import io.agrest.AgModuleProvider;
import io.agrest.encoder.PropertyMetadataEncoder;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.spi.AgExceptionMapper;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.di.spi.ModuleLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A builder of Agrest runtime. Created via {@link AgRuntime#builder()}.
 *
 * @since 5.0
 */
public class AgRuntimeBuilder {

    private final List<AgModuleProvider> moduleProviders;
    private final List<Module> modules;
    private final Map<String, AgEntityOverlay> entityOverlays;
    private final Map<String, Class<? extends AgExceptionMapper>> exceptionMappers;

    @Deprecated
    private final Map<String, PropertyMetadataEncoder> metadataEncoders;
    @Deprecated
    private String baseUrl;

    private boolean autoLoadModules;

    protected AgRuntimeBuilder() {
        this.autoLoadModules = true;
        this.entityOverlays = new HashMap<>();
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
    public AgRuntimeBuilder doNotAutoLoadModules() {
        this.autoLoadModules = false;
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
    public AgRuntimeBuilder baseUrl(String url) {
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
    public <T> AgRuntimeBuilder entityOverlay(AgEntityOverlay<T> overlay) {
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
    public AgRuntimeBuilder module(Module module) {
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
    public AgRuntimeBuilder module(AgModuleProvider provider) {
        moduleProviders.add(provider);
        return this;
    }

    /**
     * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
     */
    @Deprecated
    public AgRuntimeBuilder metadataEncoder(String type, PropertyMetadataEncoder encoder) {
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
        // Cayenne ModuleProvider provides a sorting facility, but how do we apply it across loading strategies ?

        if (autoLoadModules) {
            loadAutoLoadableModules(moduleCollector);
        }

        loadBuilderModules(moduleCollector);

        return DIBootstrap.createInjector(moduleCollector);
    }


    private Module createCoreModule() {
        return new AgCoreModule(entityOverlays, exceptionMappers, metadataEncoders, baseUrl);
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
}
