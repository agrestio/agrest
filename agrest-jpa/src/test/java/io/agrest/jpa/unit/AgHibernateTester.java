package io.agrest.jpa.unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import io.agrest.jpa.AgJpaModule;
import io.agrest.jpa.AgJpaModuleBuilder;
import io.bootique.jdbc.junit5.DbTester;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeMethodCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import jakarta.persistence.metamodel.EntityType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AgHibernateTester implements BQBeforeScopeCallback, BQAfterScopeCallback, BQBeforeMethodCallback {

    private final DbTester<?> db;
    private SessionFactory sessionFactory;

    private Collection<Class<?>> entities;

    private boolean deleteBeforeEachTest;

    public static AgHibernateTester forDb(DbTester<?> db) {
        return new AgHibernateTester(db);
    }

    private AgHibernateTester(DbTester<?> db) {
        this.db = db;
        this.deleteBeforeEachTest = false;
        this.entities = new ArrayList<>();
    }

    public AgHibernateTester deleteBeforeEachTest() {
        this.deleteBeforeEachTest = true;
        return this;
    }

    public AgHibernateTester entities(Class<?>... entities) {
        this.entities.addAll(Arrays.asList(entities));
        return this;
    }

    public AgHibernateTester entities(Collection<Class<?>> entities) {
        this.entities.addAll(entities);
        return this;
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

        // TODO: add entity sorting for the proper delete order
//        sessionFactory.getMetamodel()
    }

    @Override
    public void beforeMethod(BQTestScope scope, ExtensionContext context) {
        if(!deleteBeforeEachTest) {
            return;
        }

        Session session = sessionFactory.openSession();
        session.beginTransaction();
        try {
            for (Class<?> entity : entities) {
                EntityType<?> entityType = sessionFactory.getMetamodel().entity(entity);
                session.createQuery("delete " + entityType.getName()).executeUpdate();
            }
            session.getTransaction().commit();
        } catch (Exception ex) {
            session.getTransaction().rollback();
            throw ex;
        } finally {
            session.close();
        }
    }
}
