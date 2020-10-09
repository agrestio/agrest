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

    private NestedResourceEntity<?> entity;
    private IdReader parentIdReader;

    public NestedEntityResultReader(NestedResourceEntity<?> entity) {
        this.entity = Objects.requireNonNull(entity);
        this.parentIdReader = entity.getParent().getAgEntity().getIdReader();
    }

    @Override
    public Object value(Object root) {
        AgObjectId id = readId(root);
        return entity.getResult(id);
    }

    private AgObjectId readId(Object object) {
        // TODO: wrapping in AgObjectId seems wasteful ... Should we store results by Map ID?
        Map<String, Object> id = parentIdReader.id(object);
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
