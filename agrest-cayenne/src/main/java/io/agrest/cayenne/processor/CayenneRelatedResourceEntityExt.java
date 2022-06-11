package io.agrest.cayenne.processor;

import io.agrest.RelatedResourceEntity;
import org.apache.cayenne.query.ColumnSelect;

/**
 * Stores Cayenne-related per related entity request state. A presence in a {@link RelatedResourceEntity} tags it
 * as Cayenne-managed.
 *
 * @since 5.0
 */
public class CayenneRelatedResourceEntityExt implements CayenneResourceEntityExt {

    private ColumnSelect<Object[]> select;

    @Override
    public ColumnSelect<Object[]> getSelect() {
        return select;
    }

    public void setSelect(ColumnSelect<Object[]> select) {
        this.select = select;
    }
}
