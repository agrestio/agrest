package io.agrest.jpa.pocessor;

import java.util.Iterator;

import io.agrest.NestedResourceEntity;
import io.agrest.runtime.processor.select.SelectContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

/**
 * @since 5.0
 */
public interface IJpaQueryAssembler {
    <T> TypedQuery<T> createRootQuery(SelectContext<T> context);

    <T> Query createQueryWithParentQualifier(NestedResourceEntity<T> entity);

    <T, P> Query createQueryWithParentIdsQualifier(NestedResourceEntity<T> entity, Iterator<P> parentIt);
}
