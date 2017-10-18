package com.nhl.link.rest;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class ThrowingSupplier<T> implements Supplier<T> {

    private AtomicReference<T> value;

    public ThrowingSupplier() {
        this.value = new AtomicReference<>();
    }

    public void setValue(T value) {
        if (!this.value.compareAndSet(null, value)) {
            throw new IllegalStateException("Value has already been set");
        }
    }

    @Override
    public T get() {
        T value = this.value.get();
        if (value == null) {
            throw new IllegalStateException("Value has not been set");
        }
        return value;
    }
}
