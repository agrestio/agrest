package io.agrest.jpa.pocessor;

import java.util.Iterator;
import java.util.Map;

import io.agrest.AgObjectId;
import io.agrest.EntityParent;
import io.agrest.NestedResourceEntity;
import io.agrest.jpa.query.JpaQueryBuilder;
import io.agrest.meta.AgEntity;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 5.0
 */
public interface IJpaQueryAssembler {
    <T> JpaQueryBuilder createRootQuery(SelectContext<T> context);

    <T> JpaQueryBuilder createQueryWithParentQualifier(NestedResourceEntity<T> entity);

    <T, P> JpaQueryBuilder createQueryWithParentIdsQualifier(NestedResourceEntity<T> entity, Iterator<P> parentIt);

    JpaQueryBuilder createByIdQuery(AgEntity<?> entity, Map<String, Object> idMap);

    JpaQueryBuilder createByIdQuery(AgEntity<?> entity, AgObjectId id);

    JpaQueryBuilder createByParentIdQuery(EntityParent<?> parent);
}
