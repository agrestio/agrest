package io.agrest.unit;

import io.agrest.ResourceEntity;
import io.agrest.meta.DefaultAgAttribute;

import java.util.function.Function;

public class ResourceEntityUtils {

    public static <T, V> void appendAttribute(ResourceEntity<T> entity, String name, Class<V> valueType, Function<T, V> reader) {
        entity.addAttribute(new DefaultAgAttribute(name, valueType, true, true, o -> reader.apply((T) o)), false);
    }
}
