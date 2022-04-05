package io.agrest.jpa.pocessor;

import jakarta.persistence.TypedQuery;

/**
 * Stores JPA-related per root entity request state.
 * A presence in a {@link io.agrest.RootResourceEntity} tags it as JPA-managed.
 *
 * @since 5.0
 */
public class JpaRootResourceEntityExt<T> implements JpaResourceEntityExt {

    private TypedQuery<T> select;

    @Override
    public TypedQuery<T> getSelect() {
        return select;
    }

    public void setSelect(TypedQuery<T> select) {
        this.select = select;
    }
}
