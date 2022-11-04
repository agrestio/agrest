package io.agrest.cayenne.unit;

import io.agrest.cayenne.AgCayenneModule;
import io.agrest.cayenne.persister.CayennePersister;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.jaxrs2.junit.AgHttpTester;
import io.agrest.jaxrs2.junit.AgTestJaxrsFeature;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.AgRuntimeBuilder;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.cayenne.v42.CayenneModule;
import io.bootique.cayenne.v42.junit5.CayenneTester;
import io.bootique.command.CommandOutcome;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.jdbc.junit5.DbTester;
import io.bootique.jersey.JerseyModule;
import io.bootique.jersey.JerseyModuleExtender;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.scope.BQAfterMethodCallback;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeMethodCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.inject.Singleton;
import javax.ws.rs.client.WebTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An integration test utility that manages an application stack with a database and Cayenne runtime used for testing
 * Agrest endpoints. Under the hood combines multiple Bootique JUnit 5 tools. Users must annotate AgCayenneTester field
 * with {@link io.bootique.junit5.BQTestTool} to tie it to the JUnit 5 lifecycle.
 */
public abstract class AgCayenneTester implements BQBeforeScopeCallback, BQAfterScopeCallback, BQBeforeMethodCallback, BQAfterMethodCallback {

    protected DbTester<?> db;
    protected String cayenneProject;
    protected final List<Class<?>> resources;
    protected Class<? extends Persistent>[] entities;
    protected Class<? extends Persistent>[] entitiesAndDependencies;
    protected BiFunction<AgRuntimeBuilder, ICayennePersister, AgRuntimeBuilder> agCustomizer;
    protected boolean doNotCleanData;

    private JettyTester jettyInScope;
    private CayenneTester cayenneInScope;
    private BQRuntime appInScope;

    protected AgCayenneTester() {
        this.resources = new ArrayList<>();
        this.agCustomizer = (b, p) -> b;
        this.entities = new Class[0];
        this.entitiesAndDependencies = new Class[0];
        this.doNotCleanData = false;
    }

    public void assertQueryCount(int expected) {
        getCayenneInScope().assertQueryCount(expected);
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

    public AgRuntime runtime() {
        return getAppInScope().getInstance(AgRuntime.class);
    }

    protected CayenneTester getCayenneInScope() {
        return Objects.requireNonNull(cayenneInScope, "Not in test scope");
    }

    protected JettyTester getJettyInScope() {
        return Objects.requireNonNull(jettyInScope, "Not in test scope");
    }

    protected BQRuntime getAppInScope() {
        return Objects.requireNonNull(appInScope, "Not in test scope");
    }

    @Override
    public void beforeScope(BQTestScope scope, ExtensionContext context) throws Exception {

        this.jettyInScope = JettyTester.create();
        this.cayenneInScope = createCayenneInScope();
        this.appInScope = createAppInScope(this.jettyInScope, this.cayenneInScope);

        CommandOutcome result = appInScope.run();
        assertTrue(result.isSuccess());
        assertTrue(result.forkedToBackground());
    }

    @Override
    public void afterScope(BQTestScope scope, ExtensionContext context) {
        this.appInScope.shutdown();
        this.appInScope = null;
        this.jettyInScope = null;
        this.cayenneInScope = null;
    }

    @Override
    public void beforeMethod(BQTestScope scope, ExtensionContext context) {
        getCayenneInScope().beforeMethod(scope, context);
    }

    @Override
    public void afterMethod(BQTestScope scope, ExtensionContext context) {
        getCayenneInScope().afterMethod(scope, context);
    }

    protected CayenneTester createCayenneInScope() {
        CayenneTester tester = CayenneTester
                .create()
                .skipSchemaCreation()
                .entities(entities)
                .entitiesAndDependencies(entitiesAndDependencies);

        if (!doNotCleanData) {
            tester.deleteBeforeEachTest();
        }

        return tester;
    }

    protected BQRuntime createAppInScope(JettyTester jetty, CayenneTester cayenne) {

        Bootique builder = Bootique.app("-s")
                .autoLoadModules()
                .module(db.moduleWithTestDataSource("test"))
                .module(jetty.moduleReplacingConnectors())
                .module(cayenne.moduleWithTestHooks())
                .module(b -> CayenneModule.extend(b).addProject(cayenneProject))
                .module(new AgModule(agCustomizer, resources));

        return builder.createRuntime();
    }

    public static abstract class Builder<T extends AgCayenneTester> {

        protected final T tester;

        protected Builder(T tester) {
            this.tester = tester;
        }

        public Builder<T> db(DbTester<?> db) {
            tester.db = db;
            return this;
        }

        public Builder<T> doNotCleanData() {
            tester.doNotCleanData = true;
            return this;
        }

        @SafeVarargs
        public final Builder<T> entities(Class<? extends Persistent>... entities) {
            tester.entities = Objects.requireNonNull(entities);
            return this;
        }

        @SafeVarargs
        public final Builder<T> entitiesAndDependencies(Class<? extends Persistent>... entitiesWithDependencies) {
            tester.entitiesAndDependencies = Objects.requireNonNull(entitiesWithDependencies);
            return this;
        }

        public Builder<T> resources(Class<?>... resources) {
            Stream.of(resources).forEach(tester.resources::add);
            return this;
        }

        public Builder<T> agCustomizer(UnaryOperator<AgRuntimeBuilder> agCustomizer) {
            return agCustomizer((b, p) -> agCustomizer.apply(b));
        }

        public Builder<T> agCustomizer(BiFunction<AgRuntimeBuilder, ICayennePersister, AgRuntimeBuilder> agCustomizer) {
            tester.agCustomizer = Objects.requireNonNull(agCustomizer);
            return this;
        }

        public Builder<T> cayenneProject(String cayenneProject) {
            tester.cayenneProject = Objects.requireNonNull(cayenneProject);
            return this;
        }

        public T build() {
            Objects.requireNonNull(tester.db);
            Objects.requireNonNull(tester.cayenneProject);

            return tester;
        }
    }

    static class AgModule implements BQModule {

        private final BiFunction<AgRuntimeBuilder, ICayennePersister, AgRuntimeBuilder> customizer;
        private final List<Class<?>> resources;

        public AgModule(BiFunction<AgRuntimeBuilder, ICayennePersister, AgRuntimeBuilder> customizer, List<Class<?>> resources) {
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
        AgRuntime provideAgRuntime(ServerRuntime runtime) {
            ICayennePersister persister = new CayennePersister(runtime);
            AgRuntimeBuilder agBuilder = AgRuntime.builder()
                    .module(AgCayenneModule.builder().persister(persister).build());

            return customizer.apply(agBuilder, persister).build();
        }
    }
}
