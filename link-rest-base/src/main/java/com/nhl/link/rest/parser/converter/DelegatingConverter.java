package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.function.Supplier;

public class DelegatingConverter implements JsonValueConverter {

    private final Supplier<JsonValueConverter> converterSupplier;
    private volatile JsonValueConverter delegate;
    private final Object lock;

    public DelegatingConverter(Supplier<JsonValueConverter> converterSupplier) {
        this.converterSupplier = converterSupplier;
        this.lock = new Object();
    }

    @Override
    public Object value(JsonNode node) {
        return getDelegate().value(node);
    }

    private JsonValueConverter getDelegate() {
        if (delegate == null) {
            synchronized (lock) {
                if (delegate == null) {
                    delegate = converterSupplier.get();
                }
            }
        }
        return delegate;
    }
}
