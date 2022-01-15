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

    private final Map<AgObjectId, List<T>> resultsByParent;

    public ToManyResourceEntity(AgEntity<T> agEntity, ResourceEntity<?> parent, AgRelationship incoming) {
        super(agEntity, parent, incoming);
        this.resultsByParent = new LinkedHashMap<>();
    }

    public Map<AgObjectId, List<T>> getResultsByParent() {
        return resultsByParent;
    }

    public List<T> getResult(AgObjectId parentId) {
        return resultsByParent.get(parentId);
    }

    /**
     * @since 5.0
     */
    public List<T> getDataWindow(AgObjectId parentId) {
        // TODO: since we don't (yet) care to track the totals of the truncated relationship lists,
        //  instead of filtering a full list, we may add limits to "addResult"
        return getDataWindow(getResult(parentId));
    }

    @Override
    public void addResult(AgObjectId parentId, T object) {
        resultsByParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(object);
    }

    public void addResultList(AgObjectId parentId, List<T> objects) {
        resultsByParent.put(parentId, objects);
    }
}
