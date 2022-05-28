package io.agrest.junit;

import io.agrest.ResourceEntity;
import io.agrest.meta.DefaultAttribute;

import java.util.function.Function;

public class ResourceEntityUtils {

    public static <T, V> void appendAttribute(
            ResourceEntity<T> entity,
            String name,
            Class<V> valueType,
            Function<T, V> reader) {
        appendAttribute(entity, name, valueType, true, true, reader);
    }

    public static <T, V> void appendAttribute(
            ResourceEntity<T> entity,
            String name,
            Class<V> valueType,
            boolean readable,
            boolean writable,
            Function<T, V> reader) {
        entity.addAttribute(new DefaultAttribute(name, valueType, readable, writable, o -> reader.apply((T) o)), false);
    }
}
