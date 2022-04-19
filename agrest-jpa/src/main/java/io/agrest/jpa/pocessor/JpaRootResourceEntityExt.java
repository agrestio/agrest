package io.agrest.jpa.pocessor;

import io.agrest.jpa.query.JpaQueryBuilder;

/**
 * Stores JPA-related per root entity request state.
 * A presence in a {@link io.agrest.RootResourceEntity} tags it as JPA-managed.
 *
 * @since 5.0
 */
public class JpaRootResourceEntityExt<T> implements JpaResourceEntityExt {

    private JpaQueryBuilder select;

    @Override
    public JpaQueryBuilder getSelect() {
        return select;
    }

    public void setSelect(JpaQueryBuilder select) {
        this.select = select;
    }
}
