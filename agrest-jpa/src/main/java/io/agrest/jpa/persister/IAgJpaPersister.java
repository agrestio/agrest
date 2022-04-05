package io.agrest.jpa.persister;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Metamodel;

/**
 * @since 5.0
 */
public interface IAgJpaPersister {

    EntityManager entityManager();

    Metamodel metamodel();

}
