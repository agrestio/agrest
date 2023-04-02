package io.agrest.cayenne.processor;

import io.agrest.RelatedResourceEntity;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.exp.property.Property;
import org.apache.cayenne.query.ColumnSelect;
import org.apache.cayenne.query.ObjectSelect;

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

}

