package io.agrest.jaxrs2.junit;

import io.agrest.meta.AgEntityOverlay;

public class PojoOverlay<T> extends AgEntityOverlay<T> {

    public PojoOverlay(Class<T> type, PojoStore store) {
        super(type);
        dataResolver(new PojoRootDataResolver<>(store));
    }
}
