package io.agrest.jpa.unit;

import io.agrest.jpa.AgJpaModule;
import io.agrest.jpa.AgJpaModuleBuilder;
import io.bootique.jdbc.junit5.DbTester;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeMethodCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AgHibernateTester implements BQBeforeScopeCallback, BQAfterScopeCallback, BQBeforeMethodCallback {

    private final DbTester<?> db;
    private SessionFactory sessionFactory;

    public static AgHibernateTester forDb(DbTester<?> db) {
        return new AgHibernateTester(db);
    }

    private AgHibernateTester(DbTester<?> db) {
        this.db = db;
    }

    public AgJpaModule getJpaModule() {
        return AgJpaModuleBuilder.build(sessionFactory);
    }

    @Override
    public void afterScope(BQTestScope scope, ExtensionContext context) {
        this.sessionFactory.close();
    }

    @Override
    public void beforeScope(BQTestScope scope, ExtensionContext context) {
        // Hibernate bootstrap
        Configuration configuration = new Configuration().configure();
        // FIXME: this getter is for internal usage only, but it's the easiest way to set our datasource
        configuration.getStandardServiceRegistryBuilder().applySetting("hibernate.connection.datasource", db.getDataSource());
        this.sessionFactory = configuration.buildSessionFactory();
    }

    @Override
    public void beforeMethod(BQTestScope scope, ExtensionContext context) {

    }
}
