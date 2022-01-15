package io.agrest.property;

import io.agrest.AgObjectId;
import io.agrest.ResourceEntity;
import io.agrest.ToManyResourceEntity;

import java.util.Collections;
import java.util.List;

/**
 * A {@link PropertyReader} for to-many relationship lists that retrieves values from the child {@link ResourceEntity}
 * using the id of the parent object.
 *
 * @since 4.8
 */
public class ToManyEntityResultReader extends NestedEntityResultReader<ToManyResourceEntity<?>> {

    public ToManyEntityResultReader(ToManyResourceEntity<?> entity, PropertyReader parentIdReader) {
        super(entity, parentIdReader);
    }

    @Override
    public List<?> value(Object root) {
        AgObjectId id = readId(root);
        List<?> data = entity.getDataWindow(id);
        return data != null ? data : Collections.emptyList();
    }
}
