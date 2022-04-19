package io.agrest.jpa.pocessor;

import io.agrest.jpa.query.JpaQueryBuilder;

/**
 * Stores JPA-related per nested entity request state. A presence in a {@link io.agrest.NestedResourceEntity} tags it
 * as JPA-managed.
 *
 * @since 5.0
 */
public class JpaNestedResourceEntityExt implements JpaResourceEntityExt {

    private JpaQueryBuilder select;

    @Override
    public JpaQueryBuilder getSelect() {
        return select;
    }

    public void setSelect(JpaQueryBuilder select) {
        this.select = select;
    }
}
