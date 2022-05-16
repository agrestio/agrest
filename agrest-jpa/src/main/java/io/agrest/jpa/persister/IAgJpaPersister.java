package io.agrest.jpa.persister;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Metamodel;

/**
 * @since 5.0
 */
public interface IAgJpaPersister {

    String ENTITY_MANAGER_KEY = "entityManagerKey";

    EntityManager entityManager();

    Metamodel metamodel();

}
