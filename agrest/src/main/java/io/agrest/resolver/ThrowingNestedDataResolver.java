package io.agrest.resolver;

import io.agrest.NestedResourceEntity;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @param <T>
 * @since 3.4
 */
public class ThrowingNestedDataResolver<T> implements NestedDataResolver<T> {

    private static final NestedDataResolver instance = new ThrowingNestedDataResolver();

    public static <T> NestedDataResolver<T> getInstance() {
        return instance;
    }

    @Override
    public void onParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
        throw new UnsupportedOperationException(
                "This is a placeholder for the nested resolver. " +
                        "A real resolver is needed to read entity '" + entity.getName() + "'");
    }

    @Override
    public void onParentDataResolved(NestedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        throw new UnsupportedOperationException(
                "This is a placeholder for the nested resolver. " +
                        "A real resolver is needed to read entity '" + entity.getName() + "'");
    }
}
