package io.agrest.property;

import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;

import java.util.Collections;

/**
 * A {@link PropertyReader} for to-many relationship lists that retrieves values from the child {@link ResourceEntity}
 * using the id of the parent object.
 *
 * @since 3.4
 */
public class NestedEntityListResultReader extends NestedEntityResultReader {

    public NestedEntityListResultReader(NestedResourceEntity<?> entity) {
        super(entity);
    }

    @Override
    public Object value(Object root) {
        Object value = super.value(root);
        return value != null ? value : Collections.emptyList();
    }
}
