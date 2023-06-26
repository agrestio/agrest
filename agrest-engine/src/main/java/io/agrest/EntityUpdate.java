package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.protocol.UpdateRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private final Map<String, List<EntityUpdate<?>>> relatedUpdates;

    private Map<String, Object> id;

    @Deprecated
    private boolean explicitId;
    
    private Object mergedTo;

    public EntityUpdate(AgEntity<T> entity) {
        this.entity = Objects.requireNonNull(entity);
        this.values = new HashMap<>();
        this.relatedIds = new HashMap<>();
        this.relatedUpdates = new HashMap<>();
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
     * Returns a map of EntityUpdates for related entities. This is an experimental feature. As of now, Agrest does not
     * support processing nested updates, but this map allows the users to manually extract the values and process them
     * by hand.
     *
     * @since 5.0
     */
    public Map<String, List<EntityUpdate<?>>> getRelatedUpdates() {
        return relatedUpdates;
    }

    /**
     * @since 5.0
     */
    public void addRelatedUpdate(String relationshipName, EntityUpdate<?> update) {
        relatedUpdates.computeIfAbsent(relationshipName, n -> new ArrayList<>()).add(update);
    }

    /**
     * @since 5.0
     */
    public <R> EntityUpdate<R> getRelatedUpdate(String relationshipName) {
        List<EntityUpdate<R>> updatesList = getRelatedUpdates(relationshipName);
        return updatesList.size() == 1 ? updatesList.get(0) : null;
    }

    /**
     * @since 5.0
     */
    public <R> List<EntityUpdate<R>> getRelatedUpdates(String relationshipName) {
        List updates = relatedUpdates.get(relationshipName);
        return updates != null ? updates : Collections.emptyList();
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
     * @deprecated no longer used by Agrest to track permissions
     */
    @Deprecated(since = "5.0")
    public void setExplicitId() {
        this.explicitId = true;
    }

    /**
     * @since 1.5
     * @deprecated no longer used by Agrest to track permissions
     */
    @Deprecated(since = "5.0")
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
