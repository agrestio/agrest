package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgRelationship;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @param <T>
 * @since 3.4
 */
public class NestedResourceEntity<T> extends ResourceEntity<T> {

    private ResourceEntity<?> parent;
    private AgRelationship incoming;
    private Map<AgObjectId, Object> resultByParent;

    // TODO: Instead of AgRelationship introduce some kind of RERelationship that has references to both parent and child
    public NestedResourceEntity(
            AgEntity<T> agEntity,
            AgEntityOverlay<T> agEntityOverlay,
            ResourceEntity<?> parent,
            AgRelationship incoming) {
        
        super(agEntity, agEntityOverlay);
        this.incoming = incoming;
        this.parent = parent;
        this.resultByParent = new LinkedHashMap<>();
    }

    public ResourceEntity<?> getParent() {
        return parent;
    }

    public AgRelationship getIncoming() {
        return incoming;
    }

    public Object getResult(AgObjectId parentId) {
        return resultByParent.get(parentId);
    }

    public void setToOneResult(AgObjectId parentId, T object) {
        resultByParent.put(parentId, object);
    }

    public void addToManyResult(AgObjectId parentId, T object) {
        ((List<T>) resultByParent.computeIfAbsent(parentId, k -> new ArrayList<>())).add(object);
    }

    public void setToManyResult(AgObjectId parentId, List<T> objects) {
        resultByParent.put(parentId, objects);
    }
}
