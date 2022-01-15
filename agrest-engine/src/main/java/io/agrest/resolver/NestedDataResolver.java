package io.agrest.resolver;

import io.agrest.NestedResourceEntity;
import io.agrest.processor.ProcessingContext;
import io.agrest.property.PropertyReader;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 3.4
 */
public interface NestedDataResolver<T> {

    void onParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context);

    void onParentDataResolved(NestedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context);

    PropertyReader reader(NestedResourceEntity<T> entity, ProcessingContext<?> context);
}
