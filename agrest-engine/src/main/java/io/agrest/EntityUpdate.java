package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.protocol.UpdateRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Contains update data of a single object.
 *
 * @since 1.3
 */
public class EntityUpdate<T> implements UpdateRequest<T> {

    private final AgEntity<T> entity;
    private final Map<String, Object> values;
    private final Map<String, Set<Object>> relatedIds;

    private Map<String, Object> id;
    private boolean explicitId;
    private Object mergedTo;

    public EntityUpdate(AgEntity<T> entity) {
        this.entity = Objects.requireNonNull(entity);
        this.values = new HashMap<>();
        this.relatedIds = new HashMap<>();
    }

    /**
     * Combines this update with another update. This update is modified with data of the other update.
     *
     * @return this update
     * @since 4.8
     */
    public EntityUpdate<T> merge(EntityUpdate<T> anotherUpdate) {

        this.values.putAll(anotherUpdate.values);
        this.relatedIds.putAll(anotherUpdate.relatedIds);

        if (anotherUpdate.id != null && !anotherUpdate.id.isEmpty()) {
            getOrCreateId().putAll(anotherUpdate.id);
        }

        // If we are merging a compatible update, "explicitId", "entity", "mergedTo" should all be identical already.
        // Do not override them.

        return this;
    }

    /**
     * @since 1.19
     */
    public AgEntity<T> getEntity() {
        return entity;
    }

    public boolean hasChanges() {
        return !values.isEmpty() || !relatedIds.isEmpty();
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public Map<String, Set<Object>> getRelatedIds() {
        return relatedIds;
    }

    public void addRelatedId(String relationshipName, Object value) {
        relatedIds.computeIfAbsent(relationshipName, n -> new HashSet<>()).add(value);
    }

    /**
     * @since 1.8
     */
    public Map<String, Object> getId() {
        return id;
    }

    /**
     * @since 1.8
     */
    public Map<String, Object> getOrCreateId() {
        if (id == null) {
            id = new HashMap<>();
        }

        return id;
    }

    /**
     * @since 1.8
     */
    public void setExplicitId() {
        this.explicitId = true;
    }

    /**
     * @since 1.5
     */
    public boolean isExplicitId() {
        return explicitId;
    }

    /**
     * Returns an object that was used to merge this update to.
     *
     * @since 1.8
     */
    public Object getMergedTo() {
        return mergedTo;
    }

    /**
     * Sets an object that was used to merge this update to.
     *
     * @since 1.8
     */
    public void setMergedTo(Object mergedTo) {
        this.mergedTo = mergedTo;
    }
}
