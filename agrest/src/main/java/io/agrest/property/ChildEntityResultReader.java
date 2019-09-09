package io.agrest.property;

import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.ResourceEntity;
import io.agrest.SimpleObjectId;

import java.util.Map;
import java.util.Objects;

/**
 * A {@link PropertyReader} that retrieves values from the child {@link ResourceEntity} using the id of the parent object.
 *
 * @since 3.4
 */
public class ChildEntityResultReader implements PropertyReader {

    private ResourceEntity<?> entity;
    private IdReader idReader;

    public ChildEntityResultReader(ResourceEntity<?> entity, IdReader idReader) {
        this.entity = Objects.requireNonNull(entity);
        this.idReader = Objects.requireNonNull(idReader);
    }

    @Override
    public Object value(Object root, String name) {
        AgObjectId id = readId(root);
        return entity.getChild(name).getResult(id);
    }

    private AgObjectId readId(Object object) {
        // TODO: wrapping in AgObjectId seems wasteful ... Should we store results by Map ID?
        Map<String, Object> id = idReader.id(object);
        switch (id.size()) {
            case 0:
                throw new RuntimeException("ID is empty for '" + entity.getAgEntity() + "'");
            case 1:
                return new SimpleObjectId(id.values().iterator().next());
            default:
                return new CompoundObjectId(id);
        }
    }
}
