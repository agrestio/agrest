package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @since 4.8
 */
public class ToOneResourceEntity<T> extends NestedResourceEntity<T> {

    private final Map<AgObjectId, T> resultsByParent;

    public ToOneResourceEntity(AgEntity<T> agEntity, ResourceEntity<?> parent, AgRelationship incoming) {
        super(agEntity, parent, incoming);
        this.resultsByParent = new LinkedHashMap<>();
    }

    public Map<AgObjectId, T> getResultsByParent() {
        return resultsByParent;
    }

    public T getResult(AgObjectId parentId) {
        // TODO: apply offset/limit like ToManyResourceEntity does, only to a single object?
        return resultsByParent.get(parentId);
    }

    @Override
    public void addResult(AgObjectId parentId, T object) {
        resultsByParent.put(parentId, object);
    }
}
