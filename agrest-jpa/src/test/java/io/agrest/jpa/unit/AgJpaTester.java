package io.agrest.jpa.unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Singleton;
import javax.ws.rs.client.WebTarget;

import io.agrest.jaxrs2.junit.AgHttpTester;
import io.agrest.jaxrs2.junit.AgTestJaxrsFeature;
import io.agrest.jpa.AgJpaModule;
import io.agrest.runtime.AgRuntime;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
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
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgJpaTester implements BQBeforeScopeCallback, BQAfterScopeCallback, BQBeforeMethodCallback {

    private DbTester<?> db;
    private final List<Class<?>> resources;

    private JettyTester jettyInScope;
    private AgHibernateTester hibernateInScope;
    private BQRuntime appInScope;

    public static AgJpaTester.Builder forDb(DbTester<?> db) {
        return new Builder().db(db);
    }

    protected AgJpaTester() {
        resources = new ArrayList<>();
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

    public Table getTable(String name) {
        return db.getTable(name);
    }

    public Table e1() {
        return getTable("E1");
    }

    public Table e2() {
        return getTable("E2");
    }

    public Table e3() {
        return getTable("E3");
    }

    public Table e4() {
        return getTable("E4");
    }

    protected JettyTester getJettyInScope() {
        return Objects.requireNonNull(jettyInScope, "Not in test scope");
    }

    protected AgHibernateTester getHibernateInScope() {
        return Objects.requireNonNull(hibernateInScope, "Not in test scope");
    }

    protected BQRuntime getAppInScope() {
        return Objects.requireNonNull(appInScope, "Not in test scope");
    }

    @Override
    public void beforeScope(BQTestScope bqTestScope, ExtensionContext extensionContext) throws Exception {
        this.jettyInScope = JettyTester.create();
        this.hibernateInScope = createHibernateInScope();
        getHibernateInScope().beforeScope(bqTestScope, extensionContext);

        this.appInScope = createAppInScope(this.jettyInScope, this.hibernateInScope);

        CommandOutcome result = appInScope.run();
        assertTrue(result.isSuccess());
        assertTrue(result.forkedToBackground());
    }

    @Override
    public void afterScope(BQTestScope bqTestScope, ExtensionContext extensionContext) throws Exception {
        this.appInScope.shutdown();
        getHibernateInScope().afterScope(bqTestScope, extensionContext);
        this.appInScope = null;
        this.jettyInScope = null;
        this.hibernateInScope = null;
    }

    @Override
    public void beforeMethod(BQTestScope bqTestScope, ExtensionContext extensionContext) throws Exception {
        getHibernateInScope().beforeMethod(bqTestScope, extensionContext);
    }

    protected AgHibernateTester createHibernateInScope() {
        return AgHibernateTester.forDb(db);
    }

    protected BQRuntime createAppInScope(JettyTester jetty, AgHibernateTester hibernateInScope) {

        Bootique builder = Bootique.app("-s")
                .autoLoadModules()
                .module(db.moduleWithTestDataSource("test"))
                .module(jetty.moduleReplacingConnectors())
                .module(new AgModule(resources, hibernateInScope.getJpaModule()));

        return builder.createRuntime();
    }

    public static class Builder {

        private final AgJpaTester tester = new AgJpaTester();

        public Builder db(DbTester<?> db) {
            tester.db = db;
            return this;
        }

        public Builder resources(Class<?>... resources) {
            tester.resources.addAll(Arrays.asList(resources));
            return this;
        }

        public AgJpaTester build() {
            return tester;
        }
    }

    static class AgModule implements BQModule {

        private final List<Class<?>> resources;
        private final AgJpaModule jpaModule;

        public AgModule(List<Class<?>> resources, AgJpaModule jpaModule) {
            this.resources = resources;
            this.jpaModule = jpaModule;
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
            return AgRuntime.builder()
                    .module(jpaModule)
                    .build();
        }
    }
}
