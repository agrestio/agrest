package io.agrest;

import io.agrest.id.AgObjectId;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @since 4.8
 */
public class ToOneResourceEntity<T> extends RelatedResourceEntity<T> {

    private final Map<AgObjectId, T> dataByParent;

    public ToOneResourceEntity(AgEntity<T> agEntity, ResourceEntity<?> parent, AgRelationship incoming) {
        super(agEntity, parent, incoming);
        this.dataByParent = new LinkedHashMap<>();
    }

    /**
     * @since 5.0
     */
    public Map<AgObjectId, T> getDataByParent() {
        return dataByParent;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #getDataByParent()}
     */
    @Deprecated
    public Map<AgObjectId, T> getResultsByParent() {
        return getDataByParent();
    }

    /**
     * @since 5.0
     */
    public T getData(AgObjectId parentId) {
        // TODO: apply offset/limit like ToManyResourceEntity does, only to a single object?
        return dataByParent.get(parentId);
    }

    /**
     * @deprecated since 5.0 in favor of {@link #getData(AgObjectId)}
     */
    @Deprecated
    public T getResult(AgObjectId parentId) {
        return getData(parentId);
    }

    @Override
    public void addData(AgObjectId parentId, T object) {
        dataByParent.put(parentId, object);
    }
}
