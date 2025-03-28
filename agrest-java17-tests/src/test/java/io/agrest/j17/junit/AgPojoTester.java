package io.agrest.j17.junit;

import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.AgRuntimeBuilder;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;
import io.agrest.runtime.processor.update.CreateOrUpdateProcessorFactory;
import io.agrest.runtime.processor.update.CreateProcessorFactory;
import io.agrest.runtime.processor.update.IdempotentCreateOrUpdateProcessorFactory;
import io.agrest.runtime.processor.update.IdempotentFullSyncProcessorFactory;
import io.agrest.runtime.processor.update.UpdateProcessorFactory;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.command.CommandOutcome;
import io.bootique.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.jersey.JerseyModule;
import io.bootique.jersey.JerseyModuleExtender;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import jakarta.ws.rs.client.WebTarget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.mockito.Mockito.mock;

public class AgPojoTester implements BQBeforeScopeCallback, BQAfterScopeCallback {

    private final List<Class<?>> resources;
    private Function<AgRuntimeBuilder, AgRuntimeBuilder> agCustomizer;
    private final List<BQModule> bqModules;
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

    public WebTarget target() {
        return Objects.requireNonNull(jettyInScope, "Not in test scope").getTarget();
    }

    public AgRuntime runtime() {
        return getAppInScope().getInstance(AgRuntime.class);
    }

    protected BQRuntime getAppInScope() {
        return Objects.requireNonNull(appInScope, "Not in test scope");
    }

    @Override
    public void beforeScope(BQTestScope scope, ExtensionContext context) {
        this.jettyInScope = JettyTester.create();
        this.appInScope = createAppInScope(jettyInScope);

        CommandOutcome result = appInScope.run();
        Assertions.assertTrue(result.isSuccess(), () -> result.toString());
        Assertions.assertTrue(result.forkedToBackground());
    }

    @Override
    public void afterScope(BQTestScope scope, ExtensionContext context) {
        this.appInScope.shutdown();
        this.appInScope = null;
        this.jettyInScope = null;
    }

    protected BQRuntime createAppInScope(JettyTester jetty) {
        Bootique builder = Bootique.app("-s")
                .autoLoadModules()
                .module(jetty.moduleReplacingConnectors())
                .module(new AgModule(agCustomizer, resources));

        bqModules.forEach(builder::module);

        return builder.createRuntime();
    }

    public static class Builder {

        private final AgPojoTester tester = new AgPojoTester();

        public Builder agCustomizer(Function<AgRuntimeBuilder, AgRuntimeBuilder> agCustomizer) {
            tester.agCustomizer = Objects.requireNonNull(agCustomizer);
            return this;
        }

        public Builder resources(Class<?>... resources) {
            tester.resources.addAll(Arrays.asList(resources));
            return this;
        }

        public AgPojoTester build() {
            return tester;
        }
    }

    static class AgModule implements BQModule {

        private final Function<AgRuntimeBuilder, AgRuntimeBuilder> customizer;
        private final List<Class<?>> resources;

        public AgModule(Function<AgRuntimeBuilder, AgRuntimeBuilder> customizer, List<Class<?>> resources) {
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
            agBinder.bind(DeleteProcessorFactory.class).toInstance(mock(DeleteProcessorFactory.class));
            agBinder.bind(CreateProcessorFactory.class).toInstance(mock(CreateProcessorFactory.class));
            agBinder.bind(UpdateProcessorFactory.class).toInstance(mock(UpdateProcessorFactory.class));
            agBinder.bind(CreateOrUpdateProcessorFactory.class).toInstance(mock(CreateOrUpdateProcessorFactory.class));
            agBinder.bind(IdempotentCreateOrUpdateProcessorFactory.class).toInstance(mock(IdempotentCreateOrUpdateProcessorFactory.class));
            agBinder.bind(IdempotentFullSyncProcessorFactory.class).toInstance(mock(IdempotentFullSyncProcessorFactory.class));
            agBinder.bind(UnrelateProcessorFactory.class).toInstance(mock(UnrelateProcessorFactory.class));
        }
    }
}
