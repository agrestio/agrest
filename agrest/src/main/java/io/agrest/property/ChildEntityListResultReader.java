package io.agrest.property;

import io.agrest.ResourceEntity;

import java.util.Collections;

/**
 * A {@link PropertyReader} for to-many relationship lists that retrieves values from the child {@link ResourceEntity}
 * using the id of the parent object.
 *
 * @since 3.4
 */
public class ChildEntityListResultReader extends ChildEntityResultReader {

    public ChildEntityListResultReader(ResourceEntity<?> entity, IdReader idReader) {
        super(entity, idReader);
    }

    @Override
    public Object value(Object root, String name) {
        Object value = super.value(root, name);
        return value != null ? value : Collections.emptyList();
    }
}
