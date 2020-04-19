package io.agrest.resolver;

import io.agrest.NestedResourceEntity;
import io.agrest.property.PropertyReader;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.Collections;

/**
 * @param <T>
 * @since 3.4
 */
public class ReaderBasedResolver<T> extends BaseNestedDataResolver<T> {

    private PropertyReader reader;

    public ReaderBasedResolver(PropertyReader reader) {
        this.reader = reader;
    }

    @Override
    protected void doOnParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
        // do nothing .. parent entity will query our data for us
    }

    @Override
    protected Iterable<T> doOnParentDataResolved(NestedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        // do nothing .. parent entity will carry our data for us
        // TODO: create an iterable<T> for the sake of children
        return Collections.emptyList();
    }

    @Override
    public PropertyReader reader(NestedResourceEntity<T> entity) {
        return reader;
    }
}
