package io.agrest.jpa.persister;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Metamodel;

/**
 * @since 5.0
 */
public class AgJpaPersister implements IAgJpaPersister {

    public static final String ENTITY_MANAGER_KEY = "entityManagerKey";
    private final EntityManagerFactory entityManagerFactory;

    public AgJpaPersister(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public EntityManager entityManager() {
        // TODO: need to manage EntityManager lifecycle somehow
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public Metamodel metamodel() {
        return entityManagerFactory.getMetamodel();
    }
}
