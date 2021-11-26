package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @since 4.8
 */
public class ToOneResourceEntity<T> extends NestedResourceEntity<T> {

    private final Map<AgObjectId, T> resultByParent;

    public ToOneResourceEntity(AgEntity<T> agEntity, ResourceEntity<?> parent, AgRelationship incoming) {
        super(agEntity, parent, incoming);
        this.resultByParent = new LinkedHashMap<>();
    }

    public T getResult(AgObjectId parentId) {
        return resultByParent.get(parentId);
    }

    @Override
    public void addResult(AgObjectId parentId, T object) {
        resultByParent.put(parentId, object);
    }
}
