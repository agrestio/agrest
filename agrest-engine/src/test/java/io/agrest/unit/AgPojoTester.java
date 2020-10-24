package io.agrest.unit;

import io.agrest.pojo.model.*;
import io.agrest.pojo.runtime.PojoFetchStage;
import io.agrest.pojo.runtime.PojoSelectProcessorFactoryProvider;
import io.agrest.pojo.runtime.PojoStore;
import io.agrest.runtime.AgBuilder;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.IAgService;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;
import io.agrest.runtime.processor.update.UpdateProcessorFactoryFactory;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.command.CommandOutcome;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.jersey.JerseyModule;
import io.bootique.jersey.JerseyModuleExtender;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeMethodCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.inject.Singleton;
import javax.ws.rs.client.WebTarget;
import java.util.*;
import java.util.function.Function;

import static org.mockito.Mockito.mock;

public class AgPojoTester implements BQBeforeScopeCallback, BQAfterScopeCallback, BQBeforeMethodCallback {

    private final List<Class<?>> resources;
    private Function<AgBuilder, AgBuilder> agCustomizer;
    private final List<BQModule> bqModules;

    private PojoStore pojoStoreInScope;
    private JettyTester jettyInScope;
    private BQRuntime appInScope;

    public static Builder builder() {
        return new Builder();
    }

    protected AgPojoTester() {
        this.resources = new ArrayList<>();
        this.agCustomizer = Function.identity();
        this.bqModules = new ArrayList<>();
    }

    public AgHttpTester target() {
        return AgHttpTester.request(internalTarget());
    }

    public AgHttpTester target(String path) {
        return target().path(path);
    }

    /**
     * Provides access to JAXRS WebTarget. Used in special cases, as normally you should call {@link #target()} and
     * use the returned {@link AgHttpTester} to manage web request and run result assertions.
     */
    public WebTarget internalTarget() {
        return getJettyInScope().getTarget();
    }

    public IAgService ag() {
        return getAppInScope().getInstance(AgRuntime.class).service(IAgService.class);
    }

    public <T> Map<Object, T> bucket(Class<T> type) {
        return getPojoStoreInScope().bucket(type);
    }

    public Map<Object, P1> p1() {
        return bucket(P1.class);
    }

    public Map<Object, P2> p2() {
        return bucket(P2.class);
    }

    public Map<Object, P4> p4() {
        return bucket(P4.class);
    }

    public Map<Object, P6> p6() {
        return bucket(P6.class);
    }

    public Map<Object, P8> p8() {
        return bucket(P8.class);
    }

    public Map<Object, P9> p9() {
        return bucket(P9.class);
    }

    protected JettyTester getJettyInScope() {
        return Objects.requireNonNull(jettyInScope, "Not in test scope");
    }

    protected BQRuntime getAppInScope() {
        return Objects.requireNonNull(appInScope, "Not in test scope");
    }

    protected PojoStore getPojoStoreInScope() {
        return Objects.requireNonNull(pojoStoreInScope, "Not in test scope");
    }

    @Override
    public void beforeScope(BQTestScope scope, ExtensionContext context) {

        this.pojoStoreInScope = new PojoStore();
        this.jettyInScope = JettyTester.create();
        this.appInScope = createAppInScope(this.jettyInScope, this.pojoStoreInScope);

        CommandOutcome result = appInScope.run();
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertTrue(result.forkedToBackground());
    }

    @Override
    public void afterScope(BQTestScope scope, ExtensionContext context) {
        this.appInScope.shutdown();
        this.appInScope = null;
        this.jettyInScope = null;
        this.pojoStoreInScope = null;
    }

    @Override
    public void beforeMethod(BQTestScope scope, ExtensionContext context) {
        getPojoStoreInScope().clear();
    }

    protected BQRuntime createAppInScope(JettyTester jetty, PojoStore pojoStore) {

        Bootique builder = Bootique.app("-s")
                .autoLoadModules()
                .module(jetty.moduleReplacingConnectors())
                .module(new AgModule(agCustomizer, pojoStore, resources));

        bqModules.forEach(builder::module);

        return builder.createRuntime();
    }

    public static class Builder {

        private final AgPojoTester tester = new AgPojoTester();

        public Builder resources(Class<?>... resources) {
            tester.resources.addAll(Arrays.asList(resources));
            return this;
        }

        public Builder agCustomizer(Function<AgBuilder, AgBuilder> agCustomizer) {
            tester.agCustomizer = Objects.requireNonNull(agCustomizer);
            return this;
        }

        public Builder bqModule(BQModule module) {
            tester.bqModules.add(module);
            return this;
        }

        public AgPojoTester build() {
            return tester;
        }
    }

    static class AgModule implements BQModule {

        private final PojoStore pojoStore;
        private final Function<AgBuilder, AgBuilder> customizer;
        private final List<Class<?>> resources;

        public AgModule(Function<AgBuilder, AgBuilder> customizer, PojoStore pojoStore, List<Class<?>> resources) {
            this.pojoStore = pojoStore;
            this.customizer = customizer;
            this.resources = resources;
        }

        @Override
        public void configure(Binder binder) {
            configureJersey(JerseyModule.extend(binder));
        }

        private void configureJersey(JerseyModuleExtender extender) {
            extender.addFeature(AgRuntime.class);
            resources.forEach(extender::addResource);
        }

        @Provides
        @Singleton
        AgRuntime provideAgRuntime() {
            AgBuilder agBuilder = new AgBuilder().module(this::configureAg);
            return customizer.apply(agBuilder).build();
        }

        private void configureAg(org.apache.cayenne.di.Binder agBinder) {
            agBinder.bind(SelectProcessorFactory.class).toProvider(PojoSelectProcessorFactoryProvider.class);
            agBinder.bind(DeleteProcessorFactory.class).toInstance(mock(DeleteProcessorFactory.class));
            agBinder.bind(UpdateProcessorFactoryFactory.class).toInstance(mock(UpdateProcessorFactoryFactory.class));
            agBinder.bind(UnrelateProcessorFactory.class).toInstance(mock(UnrelateProcessorFactory.class));
            agBinder.bind(PojoFetchStage.class).to(PojoFetchStage.class);
            agBinder.bind(PojoStore.class).toInstance(pojoStore);
        }
    }
}
