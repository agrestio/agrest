package io.agrest.junit;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgSchema;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.AgRuntimeBuilder;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class AgPojoTester implements BQBeforeScopeCallback, BQAfterScopeCallback {

    private Function<AgRuntimeBuilder, AgRuntimeBuilder> agCustomizer;
    private final List<BQModule> bqModules;

    private BQRuntime appInScope;

    public static Builder builder() {
        return new Builder();
    }

    protected AgPojoTester() {
        this.agCustomizer = Function.identity();
        this.bqModules = new ArrayList<>();
    }

    public AgRuntime runtime() {
        return getAppInScope().getInstance(AgRuntime.class);
    }

    public <T> AgEntity<T> entity(Class<T> type) {
        return runtime().service(AgSchema.class).getEntity(type);
    }

    protected BQRuntime getAppInScope() {
        return Objects.requireNonNull(appInScope, "Not in test scope");
    }

    @Override
    public void beforeScope(BQTestScope scope, ExtensionContext context) {
        this.appInScope = createAppInScope();
    }

    @Override
    public void afterScope(BQTestScope scope, ExtensionContext context) {
        this.appInScope.shutdown();
        this.appInScope = null;
    }

    protected BQRuntime createAppInScope() {

        Bootique builder = Bootique.app()
                .autoLoadModules()
                .module(new AgModule(agCustomizer));

        bqModules.forEach(builder::module);

        return builder.createRuntime();
    }

    public static class Builder {

        private final AgPojoTester tester = new AgPojoTester();

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

        private final Function<AgRuntimeBuilder, AgRuntimeBuilder> customizer;

        public AgModule(Function<AgRuntimeBuilder, AgRuntimeBuilder> customizer) {
            this.customizer = customizer;
        }

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        @Singleton
        AgRuntime provideAgRuntime() {
            AgRuntimeBuilder agBuilder = AgRuntime.builder();
            return customizer.apply(agBuilder).build();
        }
    }
}
