package io.agrest.jpa.pocessor;

import java.util.Iterator;
import java.util.Map;

;
import io.agrest.RelatedResourceEntity;
import io.agrest.id.AgObjectId;
import io.agrest.jpa.query.JpaQueryBuilder;
import io.agrest.meta.AgEntity;
import io.agrest.runtime.EntityParent;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 5.0
 */
public interface IJpaQueryAssembler {
    <T> JpaQueryBuilder createRootQuery(SelectContext<T> context);

    <T> JpaQueryBuilder createQueryWithParentQualifier(RelatedResourceEntity<T> entity);

    <T, P> JpaQueryBuilder createQueryWithParentIdsQualifier(RelatedResourceEntity<T> entity, Iterator<P> parentIt);

    JpaQueryBuilder createByIdQuery(AgEntity<?> entity, Map<String, Object> idMap);

    JpaQueryBuilder createByIdQuery(AgEntity<?> entity, AgObjectId id);

    JpaQueryBuilder createByParentIdQuery(EntityParent<?> parent);
}
