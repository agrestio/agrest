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
public class ChildResourceEntity<T> extends ResourceEntity<T> {

    private AgRelationship incoming;
    private Map<AgObjectId, Object> resultByParent;

    public ChildResourceEntity(AgEntity<T> agEntity, AgEntityOverlay<T> agEntityOverlay, AgRelationship incoming) {
        super(agEntity, agEntityOverlay);
        this.incoming = incoming;
        this.resultByParent = new LinkedHashMap<>();
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
