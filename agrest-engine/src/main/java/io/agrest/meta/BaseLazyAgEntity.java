package io.agrest.meta;

import java.util.function.Supplier;

/**
 * @since 2.0
 */
public abstract class BaseLazyAgEntity<T, E extends AgEntity<T>> {

    private Supplier<E> delegateSupplier;
    private E delegate;

    public BaseLazyAgEntity(Supplier<E> delegateSupplier) {
        this.delegateSupplier = delegateSupplier;
    }

    protected final E getDelegate() {

        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    delegate = delegateSupplier.get();
                }
            }
        }
        return delegate;
    }
}
