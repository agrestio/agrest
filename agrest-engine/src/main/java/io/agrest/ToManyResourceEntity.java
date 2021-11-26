package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 4.8
 */
public class ToManyResourceEntity<T> extends NestedResourceEntity<T> {

    private final Map<AgObjectId, List<T>> resultByParent;

    public ToManyResourceEntity(AgEntity<T> agEntity, ResourceEntity<?> parent, AgRelationship incoming) {
        super(agEntity, parent, incoming);
        this.resultByParent = new LinkedHashMap<>();
    }

    public List<T> getResult(AgObjectId parentId) {
        return resultByParent.get(parentId);
    }

    @Override
    public void addResult(AgObjectId parentId, T object) {
        resultByParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(object);
    }

    public void addResultList(AgObjectId parentId, List<T> objects) {
        resultByParent.put(parentId, objects);
    }
}
