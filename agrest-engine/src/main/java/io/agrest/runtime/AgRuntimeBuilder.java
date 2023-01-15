package io.agrest.runtime;

import io.agrest.AgModuleProvider;
import io.agrest.access.MaxIncludeDepth;
import io.agrest.meta.AgEntityOverlay;
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
    private MaxIncludeDepth maxIncludeDepth;

    private boolean autoLoadModules;

    protected AgRuntimeBuilder() {
        this.autoLoadModules = true;
        this.entityOverlays = new HashMap<>();
        this.moduleProviders = new ArrayList<>(5);
        this.modules = new ArrayList<>(5);
    }

    /**
     * Suppresses module auto-loading. By default, modules are auto-loaded based on the service descriptors under
     * "META-INF/services/io.agrest.AgModuleProvider". Calling this method would suppress auto-loading behavior,
     * letting the programmer explicitly pick which extensions need to be loaded.
     *
     * @return this builder instance.
     */
    public AgRuntimeBuilder doNotAutoLoadModules() {
        this.autoLoadModules = false;
        return this;
    }

    /**
     * Adds a descriptor of extra properties of a particular entity. If multiple overlays are registered for the
     * same entity, they are merged together. If they have overlapping properties, the last overlay wins.
     *
     * @see io.agrest.SelectBuilder#entityOverlay(AgEntityOverlay)
     */
    public <T> AgRuntimeBuilder entityOverlay(AgEntityOverlay<T> overlay) {
        getOrCreateOverlay(overlay.getType()).merge(overlay);
        return this;
    }

    private <T> AgEntityOverlay<T> getOrCreateOverlay(Class<T> type) {
        // TODO: inefficiency - Cayenne DI MapBuilder only supports String keys, so we convert a Class to String
        //  here, and then convert a String to Class when using the overlays in the compiler
        return entityOverlays.computeIfAbsent(type.getName(), n -> new AgEntityOverlay<>(type));
    }

    /**
     * Registers a DI extension module for {@link AgRuntime}.
     *
     * @param module an extension DI module for {@link AgRuntime}.
     * @return this builder instance.
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
     */
    public AgRuntimeBuilder module(AgModuleProvider provider) {
        moduleProviders.add(provider);
        return this;
    }

    /**
     * Sets the policy for the maximum depth of relationship includes. The policy is applied to select and update
     * requests. Includes are counted from the root of the request. Only non-negative depth are allowed. Zero depth
     * blocks all relationships, "1" - blocks anything beyond direct relationships, and so on. This policy does not
     * affect attribute includes (either root or nested).
     *
     * @return this builder instance
     * @since 5.0
     */
    public AgRuntimeBuilder maxIncludeDepth(int maxIncludeDepth) {
        this.maxIncludeDepth = MaxIncludeDepth.of(maxIncludeDepth);
        return this;
    }

    public AgRuntime build() {
        return new AgRuntime(createInjector());
    }

    private Injector createInjector() {

        Collection<Module> modules = new ArrayList<>();

        // core module goes first, the rest of modules override the core and each other
        modules.add(createCoreModule());

        // TODO: consistent sorting policy past core module...
        // Cayenne ModuleProvider provides a sorting facility, but how do we apply it across loading strategies ?

        if (autoLoadModules) {
            loadAutoLoadableModules(modules);
        }

        loadBuilderModules(modules);

        return DIBootstrap.createInjector(modules);
    }


    private Module createCoreModule() {
        return new AgCoreModule(
                entityOverlays,
                maxIncludeDepth != null ? maxIncludeDepth : MaxIncludeDepth.ofDefault());
    }

    private void loadAutoLoadableModules(Collection<Module> collector) {
        collector.addAll(new ModuleLoader().load(AgModuleProvider.class));
    }

    private void loadBuilderModules(Collection<Module> modules) {

        // TODO: Should we enforce addition order between modules and providers?

        modules.addAll(this.modules);
        moduleProviders.forEach(p -> modules.add(p.module()));
    }
}
