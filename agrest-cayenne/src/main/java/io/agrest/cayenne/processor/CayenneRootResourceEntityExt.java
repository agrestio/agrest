package io.agrest.cayenne.processor;

import org.apache.cayenne.query.ObjectSelect;

/**
 * Stores Cayenne-related per root entity request state. A presence in a {@link io.agrest.RootResourceEntity} tags it
 * as Cayenne-managed.
 *
 * @since 5.0
 */
public class CayenneRootResourceEntityExt<T> implements CayenneResourceEntityExt {

    private ObjectSelect<T> select;

    @Override
    public ObjectSelect<T> getSelect() {
        return select;
    }

    public void setSelect(ObjectSelect<T> select) {
        this.select = select;
    }
}
