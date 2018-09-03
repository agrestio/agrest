package io.agrest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.function.Supplier;

public class LazyConverter<T> implements JsonValueConverter<T> {

    private final Supplier<JsonValueConverter<T>> delegateSupplier;
    private volatile JsonValueConverter<T> delegate;
    private final Object lock;

    public LazyConverter(Supplier<JsonValueConverter<T>> delegateSupplier) {
        this.delegateSupplier = delegateSupplier;
        this.lock = new Object();
    }

    @Override
    public T value(JsonNode node) {
        if (delegate == null) {
            synchronized (lock) {
                if (delegate == null) {
                    delegate = delegateSupplier.get();
                }
            }
        }
        return delegate.value(node);
    }
}
