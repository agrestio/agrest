package io.agrest.reader;

import io.agrest.id.AgObjectId;
import io.agrest.ResourceEntity;
import io.agrest.ToManyResourceEntity;

import java.util.Collections;
import java.util.List;

/**
 * A {@link DataReader} for to-many relationship lists that retrieves values from the child {@link ResourceEntity}
 * using the id of the parent object.
 *
 * @since 4.8
 */
public class ToManyEntityResultReader extends RelatedEntityResultReader<ToManyResourceEntity<?>> {

    public ToManyEntityResultReader(ToManyResourceEntity<?> entity, DataReader parentIdReader) {
        super(entity, parentIdReader);
    }

    @Override
    public List<?> read(Object root) {
        AgObjectId id = readId(root);
        List<?> data = entity.getDataWindow(id);
        return data != null ? data : Collections.emptyList();
    }
}
