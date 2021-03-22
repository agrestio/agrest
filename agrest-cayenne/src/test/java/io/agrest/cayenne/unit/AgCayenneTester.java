package io.agrest.cayenne.unit;

import io.agrest.cayenne.AgCayenneBuilder;
import io.agrest.cayenne.persister.CayennePersister;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.runtime.AgBuilder;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.IAgService;
import io.agrest.unit.AgHttpTester;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.cayenne.v42.CayenneModule;
import io.bootique.cayenne.v42.junit5.CayenneTester;
import io.bootique.command.CommandOutcome;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.jdbc.junit5.DbTester;
import io.bootique.jdbc.junit5.Table;
import io.bootique.jersey.JerseyModule;
import io.bootique.jersey.JerseyModuleExtender;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQTestScope;
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
public class AgCayenneTester implements BQBeforeScopeCallback, BQAfterScopeCallback, BQBeforeMethodCallback {

    private DbTester<?> db;
    private String cayenneProject;
    private final List<Class<?>> resources;
    private Class<? extends Persistent>[] entities;
    private Class<? extends Persistent>[] entitiesAndDependencies;
    private BiFunction<AgBuilder, ICayennePersister, AgBuilder> agCustomizer;
    private boolean doNotCleanData;

    private JettyTester jettyInScope;
    private CayenneTester cayenneInScope;
    private BQRuntime appInScope;

    public static AgCayenneTester.Builder forDb(DbTester<?> db) {
        return new Builder().db(db);
    }

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

    public IAgService ag() {
        return getAppInScope().getInstance(AgRuntime.class).service(IAgService.class);
    }

    public Table e1() {
        return db.getTable("e1");
    }

    public Table e2() {
        return db.getTable("e2");
    }

    public Table e3() {
        return db.getTable("e3");
    }

    public Table e4() {
        return db.getTable("e4");
    }

    public Table e5() {
        return db.getTable("e5");
    }

    public Table e6() {
        return db.getTable("e6");
    }

    public Table e7() {
        return db.getTable("e7");
    }

    public Table e8() {
        return db.getTable("e8");
    }

    public Table e9() {
        return db.getTable("e9");
    }

    public Table e10() {
        return db.getTable("e10");
    }

    public Table e11() {
        return db.getTable("e11");
    }

    public Table e12() {
        return db.getTable("e12");
    }

    public Table e13() {
        return db.getTable("e13");
    }

    public Table e12_13() {
        return db.getTable("e12_e13");
    }

    public Table e14() {
        return db.getTable("e14");
    }

    public Table e15() {
        return db.getTable("e15");
    }

    public Table e15_1() {
        return db.getTable("e15_e1");
    }

    public Table e15_5() {
        return db.getTable("e15_e5");
    }

    public Table e17() {
        return db.getTable("e17");
    }

    public Table e18() {
        return db.getTable("e18");
    }

    public Table e19() {
        return db.getTable("e19");
    }

    public Table e20() {
        return db.getTable("e20");
    }

    public Table e21() {
        return db.getTable("e21");
    }

    public Table e22() {
        return db.getTable("e22");
    }

    public Table e23() {
        return db.getTable("e23");
    }

    public Table e24() {
        return db.getTable("e24");
    }

    public Table e25() {
        return db.getTable("e25");
    }

    public Table e26() {
        return db.getTable("e26");
    }

    public Table e27NoPk() {
        return db.getTable("e27_nopk");
    }

    public Table e28() {
        return db.getTable("e28");
    }

    public Table e29() {
        return db.getTable("e29");
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
    public void beforeScope(BQTestScope scope, ExtensionContext context) {

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

    public static class Builder {

        private final AgCayenneTester tester = new AgCayenneTester();

        public Builder db(DbTester<?> db) {
            tester.db = db;
            return this;
        }

        public Builder doNotCleanData() {
            tester.doNotCleanData = true;
            return this;
        }

        @SafeVarargs
        public final Builder entities(Class<? extends Persistent>... entities) {
            tester.entities = Objects.requireNonNull(entities);
            return this;
        }

        @SafeVarargs
        public final Builder entitiesAndDependencies(Class<? extends Persistent>... entitiesWithDependencies) {
            tester.entitiesAndDependencies = Objects.requireNonNull(entitiesWithDependencies);
            return this;
        }

        public Builder resources(Class<?>... resources) {
            Stream.of(resources).forEach(tester.resources::add);
            return this;
        }

        public Builder agCustomizer(UnaryOperator<AgBuilder> agCustomizer) {
            return agCustomizer((b, p) -> agCustomizer.apply(b));
        }

        public Builder agCustomizer(BiFunction<AgBuilder, ICayennePersister, AgBuilder> agCustomizer) {
            tester.agCustomizer = Objects.requireNonNull(agCustomizer);
            return this;
        }

        public Builder cayenneProject(String cayenneProject) {
            tester.cayenneProject = Objects.requireNonNull(cayenneProject);
            return this;
        }

        public AgCayenneTester build() {
            Objects.requireNonNull(tester.db);
            Objects.requireNonNull(tester.cayenneProject);

            return tester;
        }
    }

    static class AgModule implements BQModule {

        private final BiFunction<AgBuilder, ICayennePersister, AgBuilder> customizer;
        private final List<Class<?>> resources;

        public AgModule(BiFunction<AgBuilder, ICayennePersister, AgBuilder> customizer, List<Class<?>> resources) {
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
        AgRuntime provideAgRuntime(ServerRuntime runtime) {
            ICayennePersister persister = new CayennePersister(runtime);
            AgBuilder agBuilder = new AgBuilder().module(
                    AgCayenneBuilder.builder().persister(persister).build());
            return customizer.apply(agBuilder, persister).build();
        }
    }
}
