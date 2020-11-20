package io.agrest.property;

import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.NestedResourceEntity;
import io.agrest.SimpleObjectId;

import java.util.Map;
import java.util.Objects;

/**
 * A {@link PropertyReader} that retrieves values from a {@link NestedResourceEntity} using the id of the parent object.
 *
 * @since 3.4
 */
public class NestedEntityResultReader implements PropertyReader {

    private final NestedResourceEntity<?> entity;

    // note that we are referring to the parent reader as a "key" reader, not "id" reader, as it may not align with
    // AgEntity ID (as it is used for a different purpose, which is to uniquely identify the parent from child select data).
    private final PropertyReader parentKeyReader;

    public NestedEntityResultReader(NestedResourceEntity<?> entity, PropertyReader parentKeyReader) {
        this.entity = Objects.requireNonNull(entity);
        this.parentKeyReader = parentKeyReader;
    }

    @Override
    public Object value(Object root) {
        AgObjectId id = readId(root);
        return entity.getResult(id);
    }

    private AgObjectId readId(Object object) {
        // TODO: wrapping in AgObjectId is wasteful
        Map<String, Object> id = (Map<String, Object>) parentKeyReader.value(object);
        switch (id.size()) {
            case 0:
                throw new RuntimeException("ID is empty for '" + entity.getName() + "'");
            case 1:
                return new SimpleObjectId(id.values().iterator().next());
            default:
                return new CompoundObjectId(id);
        }
    }
}
