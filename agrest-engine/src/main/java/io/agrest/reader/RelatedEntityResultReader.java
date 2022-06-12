package io.agrest.reader;

import io.agrest.AgObjectId;
import io.agrest.RelatedResourceEntity;

import java.util.Map;
import java.util.Objects;

/**
 * A {@link DataReader} that retrieves values from a {@link RelatedResourceEntity} using the id of the parent object.
 *
 * @since 3.4
 */
public abstract class RelatedEntityResultReader<T extends RelatedResourceEntity<?>> implements DataReader {

    protected final T entity;

    // note that we are referring to the parent reader as a "key" reader, not "id" reader, as it may not align with
    // AgEntity ID (as it is used for a different purpose, which is to uniquely identify the parent from child select data).
    private final DataReader parentKeyReader;

    public RelatedEntityResultReader(T entity, DataReader parentKeyReader) {
        this.entity = Objects.requireNonNull(entity);
        this.parentKeyReader = parentKeyReader;
    }

    protected AgObjectId readId(Object object) {
        // TODO: wrapping in AgObjectId is wasteful
        Map<String, Object> id = (Map<String, Object>) parentKeyReader.read(object);
        switch (id.size()) {
            case 0:
                throw new RuntimeException("ID is empty for '" + entity.getName() + "'");
            case 1:
                return AgObjectId.of(id.values().iterator().next());
            default:
                return AgObjectId.ofMap(id);
        }
    }
}
