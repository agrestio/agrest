package io.agrest.cayenne.processor;

import org.apache.cayenne.query.SelectQuery;

/**
 * Cayenne extensions of a {@link io.agrest.ResourceEntity} stored in entity properties that tags the entity as
 * Cayenne-managed and provides Cayenne-related entity properties, such as query, etc.
 *
 * @since 4.8
 */
public class CayenneResourceEntityExt<T> {

    private SelectQuery<T> select;

    // TODO: generics here are not accurate. The actual query is a column query for nested entities and an
    //  object query for root
    public SelectQuery<T> getSelect() {
        return select;
    }

    public void setSelect(SelectQuery<T> select) {
        this.select = select;
    }

    public boolean hasSelect() {
        return select != null;
    }
}
