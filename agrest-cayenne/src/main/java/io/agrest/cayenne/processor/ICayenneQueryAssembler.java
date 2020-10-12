package io.agrest.cayenne.processor;

import io.agrest.AgObjectId;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.query.SelectQuery;

import java.util.Iterator;

/**
 * @since 3.7
 */
public interface ICayenneQueryAssembler {

    <T> SelectQuery<T> createRootQuery(SelectContext<T> context);

    <T> SelectQuery<T> createRootIdQuery(ResourceEntity<T> entity, AgObjectId rootId);

    <T> SelectQuery<T> createQueryWithParentQualifier(NestedResourceEntity<T> entity);

    <T, P> SelectQuery<T> createQueryWithParentIdsQualifier(NestedResourceEntity<T> entity, Iterator<P> parentData);

}
