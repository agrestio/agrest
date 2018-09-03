package io.agrest.meta.compiler;

import io.agrest.meta.AgEntity;

import java.util.function.Supplier;

/**
 * @since 2.0
 */
public abstract class BaseLazyLrEntity<T, E extends AgEntity<T>> {

    private Supplier<E> delegateSupplier;
    private E delegate;
    private final Object lock;

    public BaseLazyLrEntity(Supplier<E> delegateSupplier) {
        this.delegateSupplier = delegateSupplier;
        lock = new Object();
    }

    protected final E getDelegate() {

        if (delegate == null) {
            synchronized (lock) {
                if (delegate == null) {
                    delegate = delegateSupplier.get();
                }
            }
        }
        return delegate;
    }
}
