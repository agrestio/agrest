package io.agrest.cayenne.processor;

import io.agrest.RelatedResourceEntity;
import io.agrest.id.AgObjectId;
import io.agrest.meta.AgEntity;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.exp.property.Property;
import org.apache.cayenne.query.ColumnSelect;
import org.apache.cayenne.query.ObjectSelect;

import java.util.Collection;

/**
 * @since 3.7
 */
public interface ICayenneQueryAssembler {

    <T> ObjectSelect<T> createRootQuery(SelectContext<T> context);

    <T> ColumnSelect<Object[]> createQueryWithParentQualifier(RelatedResourceEntity<T> entity);

    <T, P> ColumnSelect<Object[]> createQueryWithParentIdsQualifier(
            RelatedResourceEntity<T> entity,
            Iterable<P> parentData);

    <T> Property<?>[] queryColumns(RelatedResourceEntity<T> entity);

    /**
     * @since 5.0
     */
    <T> ObjectSelect<T> createQueryForIds(AgEntity<T> entity, Collection<AgObjectId> ids);

}

