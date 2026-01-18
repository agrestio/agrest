package io.agrest;

import io.agrest.id.AgObjectId;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 4.8
 */
public class ToManyResourceEntity<T> extends RelatedResourceEntity<T> {

    private final Map<AgObjectId, List<T>> dataByParent;

    public ToManyResourceEntity(AgEntity<T> agEntity, ResourceEntity<?> parent, AgRelationship incoming) {
        super(agEntity, parent, incoming);
        this.dataByParent = new LinkedHashMap<>();
    }

    /**
     * @since 5.0
     */
    public Map<AgObjectId, List<T>> getDataByParent() {
        return dataByParent;
    }

    /**
     * @since 5.0
     */
    public List<T> getData(AgObjectId parentId) {
        return dataByParent.get(parentId);
    }

    /**
     * @since 5.0
     */
    public List<T> getDataWindow(AgObjectId parentId) {
        // TODO: since we don't (yet) care to track totals of the truncated relationship lists,
        //  instead of filtering a full list, we may add limits to "addResult"
        return getDataWindow(getData(parentId));
    }

    @Override
    public void addData(AgObjectId parentId, T object) {
        dataByParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(object);
    }

    /**
     * @since 5.0
     */
    public void setData(AgObjectId parentId, List<T> data) {
        dataByParent.put(parentId, data);
    }
}
