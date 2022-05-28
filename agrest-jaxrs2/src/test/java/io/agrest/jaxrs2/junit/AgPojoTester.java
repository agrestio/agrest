package io.agrest.jaxrs2.junit;

import io.agrest.jaxrs2.pojo.model.P1;
import io.agrest.jaxrs2.pojo.model.P10;
import io.agrest.jaxrs2.pojo.model.P2;
import io.agrest.jaxrs2.pojo.model.P4;
import io.agrest.jaxrs2.pojo.model.P6;
import io.agrest.jaxrs2.pojo.model.P8;
import io.agrest.jaxrs2.pojo.model.P9;
import io.agrest.jaxrs2.pojo.runtime.PojoFetchStage;
import io.agrest.jaxrs2.pojo.runtime.PojoSelectProcessorFactoryProvider;
import io.agrest.jaxrs2.pojo.runtime.PojoStore;
import io.agrest.meta.AgSchema;
import io.agrest.meta.AgEntity;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.AgRuntimeBuilder;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;
import io.agrest.runtime.processor.update.CreateOrUpdateProcessorFactory;
import io.agrest.runtime.processor.update.CreateProcessorFactory;
import io.agrest.runtime.processor.update.IdempotentCreateOrUpdateProcessorFactory;
import io.agrest.runtime.processor.update.IdempotentFullSyncProcessorFactory;
import io.agrest.runtime.processor.update.UpdateProcessorFactory;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static org.mockito.Mockito.mock;

public class AgPojoTester implements BQBeforeScopeCallback, BQAfterScopeCallback, BQBeforeMethodCallback {

    private final List<Class<?>> resources;
    private Function<AgRuntimeBuilder, AgRuntimeBuilder> agCustomizer;
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
     * Provides access to JAX-RS WebTarget. Used in special cases, as normally you should call {@link #target()} and
     * use the returned {@link AgHttpTester} to manage web request and run result assertions.
     */
    public WebTarget internalTarget() {
        return getJettyInScope().getTarget();
    }

    public AgRuntime runtime() {
        return getAppInScope().getInstance(AgRuntime.class);
    }

    public <T> AgEntity<T> entity(Class<T> type) {
        return runtime().service(AgSchema.class).getEntity(type);
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

    public Map<Object, P10> p10() {
        return bucket(P10.class);
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
        Assertions.assertTrue(result.isSuccess(), () -> result.toString());
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

        public Builder agCustomizer(Function<AgRuntimeBuilder, AgRuntimeBuilder> agCustomizer) {
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
        private final Function<AgRuntimeBuilder, AgRuntimeBuilder> customizer;
        private final List<Class<?>> resources;

        public AgModule(Function<AgRuntimeBuilder, AgRuntimeBuilder> customizer, PojoStore pojoStore, List<Class<?>> resources) {
            this.pojoStore = pojoStore;
            this.customizer = customizer;
            this.resources = resources;
        }

        @Override
        public void configure(Binder binder) {
            configureJersey(JerseyModule.extend(binder));
        }

        private void configureJersey(JerseyModuleExtender extender) {
            extender.addFeature(AgTestJaxrsFeature.class);
            resources.forEach(extender::addResource);
        }

        @Provides
        @Singleton
        AgRuntime provideAgRuntime() {
            AgRuntimeBuilder agBuilder = AgRuntime.builder().module(this::configureAg);
            return customizer.apply(agBuilder).build();
        }

        private void configureAg(org.apache.cayenne.di.Binder agBinder) {
            agBinder.bind(SelectProcessorFactory.class).toProvider(PojoSelectProcessorFactoryProvider.class);
            agBinder.bind(DeleteProcessorFactory.class).toInstance(mock(DeleteProcessorFactory.class));
            agBinder.bind(CreateProcessorFactory.class).toInstance(mock(CreateProcessorFactory.class));
            agBinder.bind(UpdateProcessorFactory.class).toInstance(mock(UpdateProcessorFactory.class));
            agBinder.bind(CreateOrUpdateProcessorFactory.class).toInstance(mock(CreateOrUpdateProcessorFactory.class));
            agBinder.bind(IdempotentCreateOrUpdateProcessorFactory.class).toInstance(mock(IdempotentCreateOrUpdateProcessorFactory.class));
            agBinder.bind(IdempotentFullSyncProcessorFactory.class).toInstance(mock(IdempotentFullSyncProcessorFactory.class));
            agBinder.bind(UnrelateProcessorFactory.class).toInstance(mock(UnrelateProcessorFactory.class));
            agBinder.bind(PojoFetchStage.class).to(PojoFetchStage.class);
            agBinder.bind(PojoStore.class).toInstance(pojoStore);
        }
    }
}
