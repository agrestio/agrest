package io.agrest.jpa.pocessor;

import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.runtime.processor.select.SelectContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaQueryAssembler implements IJpaQueryAssembler {

    private final IAgJpaPersister persister;

    public JpaQueryAssembler(@Inject IAgJpaPersister persister) {
        this.persister = persister;
    }

    @Override
    public <T> TypedQuery<T> createRootQuery(SelectContext<T> context) {
        EntityManager entityManager = persister.entityManager();
        // TODO: using JPA query syntax here, may need something better
        //       also no EntityManager lifecycle management here
        return entityManager.createQuery("select e from " + context.getEntity().getName() + " e", context.getEntity().getType());
    }
}
