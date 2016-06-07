package com.nhl.link.rest.meta.compiler;

import com.nhl.link.rest.meta.LrEntity;

import java.util.function.Supplier;

/**
 * @since 2.0
 */
public abstract class BaseLazyLrEntity<T, E extends LrEntity<T>> {

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
