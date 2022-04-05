package io.agrest.jpa.pocessor;

import jakarta.persistence.Query;

/**
 * Stores JPA-related per nested entity request state. A presence in a {@link io.agrest.NestedResourceEntity} tags it
 * as JPA-managed.
 *
 * @since 5.0
 */
public class JpaNestedResourceEntityExt implements JpaResourceEntityExt {

    private Query select; // TODO: ???

    @Override
    public Query getSelect() {
        return select;
    }

    public void setSelect(Query select) {
        this.select = select;
    }
}
