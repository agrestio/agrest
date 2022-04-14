package io.agrest.jpa.pocessor;

import java.util.Iterator;

import io.agrest.NestedResourceEntity;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.meta.AgRelationship;
import io.agrest.runtime.processor.select.SelectContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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

    @Override
    public <T> Query createQueryWithParentQualifier(NestedResourceEntity<T> entity) {
        EntityManager entityManager = persister.entityManager();


        AgRelationship incoming = entity.getIncoming();
        // TODO: need parent id here
        return entityManager.createQuery("select e from " + entity.getName() + " e");
    }

    @Override
    public <T, P> Query createQueryWithParentIdsQualifier(NestedResourceEntity<T> entity, Iterator<P> parentIt) {
        return null;
    }
}
