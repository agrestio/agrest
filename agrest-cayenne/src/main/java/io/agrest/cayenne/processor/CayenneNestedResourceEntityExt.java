package io.agrest.cayenne.processor;

import org.apache.cayenne.query.ColumnSelect;

/**
 * Stores Cayenne-related per nested entity request state. A presence in a {@link io.agrest.NestedResourceEntity} tags it
 * as Cayenne-managed.
 *
 * @since 5.0
 */
public class CayenneNestedResourceEntityExt implements CayenneResourceEntityExt {

    private ColumnSelect<Object[]> select;

    @Override
    public ColumnSelect<Object[]> getSelect() {
        return select;
    }

    public void setSelect(ColumnSelect<Object[]> select) {
        this.select = select;
    }
}
