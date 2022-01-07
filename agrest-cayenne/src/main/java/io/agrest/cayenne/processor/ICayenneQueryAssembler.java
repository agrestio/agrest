package io.agrest.cayenne.processor;

import io.agrest.NestedResourceEntity;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.exp.property.Property;
import org.apache.cayenne.query.ColumnSelect;
import org.apache.cayenne.query.ObjectSelect;

import java.util.Iterator;

/**
 * @since 3.7
 */
public interface ICayenneQueryAssembler {

    <T> ObjectSelect<T> createRootQuery(SelectContext<T> context);

    <T> ColumnSelect<Object[]> createQueryWithParentQualifier(NestedResourceEntity<T> entity);

    <T, P> ColumnSelect<Object[]> createQueryWithParentIdsQualifier(
            NestedResourceEntity<T> entity,
            Iterator<P> parentData);

    <T> Property<?>[] queryColumns(NestedResourceEntity<T> entity);

}

