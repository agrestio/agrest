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
 * A mutable object with update data of a single model object. For to-many relationships makes a distinction between
 * "no update present" (null collection) and "all related objects should be removed" (empty collection).
 *
 * @since 1.3
 */
public class EntityUpdate<T> implements UpdateRequest<T> {

    private final AgEntity<T> entity;

    private Map<String, Object> idParts;
    private Map<String, Object> attributes;
    private Map<String, EntityUpdate<?>> toOnes;
    private Map<String, List<EntityUpdate<?>>> toManys;
    private Map<String, Object> toOneIds;
    private Map<String, Set<Object>> toManyIds;

    private T targetObject;

    public EntityUpdate(AgEntity<T> entity) {
        this.entity = Objects.requireNonNull(entity);
    }

    /**
     * Combines this update with another update. This update is modified with data of the other update.
     *
     * @return this update
     * @since 4.8
     */
    public EntityUpdate<T> merge(EntityUpdate<T> anotherUpdate) {

        this.idParts = mergeMaps(idParts, anotherUpdate.idParts);
        this.attributes = mergeMaps(attributes, anotherUpdate.attributes);
        this.toOnes = mergeMaps(toOnes, anotherUpdate.toOnes);
        this.toManys = mergeMaps(toManys, anotherUpdate.toManys);
        this.toOneIds = mergeMaps(toOneIds, anotherUpdate.toOneIds);
        this.toManyIds = mergeMaps(toManyIds, anotherUpdate.toManyIds);

        // We are presumably merging a compatible update, so "entity", "targetObject" should all be identical and
        // require no override

        return this;
    }

    /**
     * @since 1.19
     */
    public AgEntity<T> getEntity() {
        return entity;
    }

    /**
     * Returns an object targeted by this update. The object is null until it is initialized by Agrest, usually during
     * {@link UpdateStage#MERGE_CHANGES} step of the pipeline.
     *
     * @since 5.0
     */
    public T getTargetObject() {
        return targetObject;
    }

    /**
     * Sets an object targeted by this update.
     *
     * @since 5.0
     */
    public void setTargetObject(T targetObject) {
        this.targetObject = targetObject;
    }

    /**
     * @since 5.0
     */
    public Map<String, Object> getIdParts() {
        return idParts != null ? idParts : Collections.emptyMap();
    }

    /**
     * @since 5.0
     */
    public Object getIdPart(String idPart) {
        return idParts != null ? idParts.get(idPart) : null;
    }

    /**
     * @since 5.0
     */
    public void setIdParts(Map<String, Object> id) {
        mutableIdParts().putAll(id);
    }

    /**
     * @since 5.0
     */
    public void addIdPart(String idPart, Object value) {
        mutableIdParts().put(idPart, value);
    }

    /**
     * @since 5.0
     */
    public void addIdPartIfAbsent(String idPart, Object value) {
        mutableIdParts().putIfAbsent(idPart, value);
    }

    /**
     * @since 5.0
     */
    public Map<String, Object> getAttributes() {
        return attributes != null ? attributes : Collections.emptyMap();
    }

    public Object getAttribute(String attribute) {
        return attributes != null ? attributes.get(attribute) : null;
    }

    /**
     * @since 5.0
     */
    public void setAttribute(String attribute, Object value) {
        mutableAttributes().put(attribute, value);
    }

    /**
     * @since 5.0
     */
    public Map<String, EntityUpdate<?>> getToOnes() {
        return toOnes != null ? toOnes : Collections.emptyMap();
    }

    /**
     * @since 5.0
     */
    public <R> EntityUpdate<R> getToOne(String relationship) {
        return toOnes != null ? (EntityUpdate<R>) toOnes.get(relationship) : null;
    }

    /**
     * @since 5.0
     */
    public void setToOne(String relationshipName, EntityUpdate<?> update) {
        mutableToOnes().put(relationshipName, update);
    }

    /**
     * Returns a map of EntityUpdates for related entities. This is an experimental feature. As of now, Agrest does not
     * support processing nested updates, but this map allows the users to manually extract the values and process them
     * by hand.
     *
     * @since 5.0
     */
    public Map<String, List<EntityUpdate<?>>> getToManys() {
        return toManys != null ? toManys : Collections.emptyMap();
    }

    /**
     * @since 5.0
     */
    public <R> List<EntityUpdate<R>> getToMany(String relationship) {
        // Do not return empty list of the key is not there. We must distinguish between no update and empty list.
        // Empty list may mean "remove all target objects".
        return toManys != null ? (List) toManys.get(relationship) : null;
    }

    /**
     * @since 5.0
     */
    public void addToMany(String relationship, EntityUpdate<?> update) {
        mutableToManys().computeIfAbsent(relationship, n -> new ArrayList<>()).add(update);
    }

    /**
     * A mutator that resets the update to a state indicating an explicit need to remove all related objects.
     * This will result in an empty array for related objects, which is different from a null array.
     *
     * @since 5.0
     */
    public void emptyToMany(String relationship) {
        mutableToManys().computeIfAbsent(relationship, n -> new ArrayList<>()).clear();
    }

    /**
     * @since 5.0
     */
    public Map<String, Object> getToOneIds() {
        return toOneIds != null ? toOneIds : Collections.emptyMap();
    }

    /**
     * @since 5.0
     */
    public Object getToOneId(String relationship) {
        return toOneIds != null ? toOneIds.get(relationship) : null;
    }

    /**
     * @since 5.0
     */
    public void setToOneId(String relationship, Object value) {
        mutableToOneIds().put(relationship, value);
    }

    /**
     * @since 5.0
     */
    public Map<String, Set<Object>> getToManyIds() {
        return toManyIds != null ? toManyIds : Collections.emptyMap();
    }

    /**
     * @since 5.0
     */
    public Set<Object> getToManyIds(String relationship) {
        return toManyIds != null ? toManyIds.get(relationship) : null;
    }

    /**
     * @since 5.0
     */
    public void addToManyId(String relationship, Object value) {
        mutableToManyIds().computeIfAbsent(relationship, n -> new HashSet<>()).add(value);
    }

    /**
     * A mutator that resets the update to a state indicating an explicit need to remove all related ids.
     * This will result in an empty array for ids, which is different from a null array.
     *
     * @since 5.0
     */
    public void emptyToManyIds(String relationship) {
        mutableToManyIds().computeIfAbsent(relationship, n -> new HashSet<>()).clear();
    }

    private <M> Map<String, M> mergeMaps(Map<String, M> to, Map<String, M> from) {
        if (from == null || from.isEmpty()) {
            return to;
        }

        if (to != null) {
            to.putAll(from);
            return to;
        }

        return new HashMap<>(from);
    }

    private Map<String, Object> mutableIdParts() {
        if (idParts == null) {
            idParts = new HashMap<>();
        }

        return idParts;
    }

    private Map<String, Object> mutableAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        return attributes;
    }

    private Map<String, EntityUpdate<?>> mutableToOnes() {
        if (toOnes == null) {
            toOnes = new HashMap<>();
        }

        return toOnes;
    }

    private Map<String, List<EntityUpdate<?>>> mutableToManys() {
        if (toManys == null) {
            toManys = new HashMap<>();
        }

        return toManys;
    }

    private Map<String, Object> mutableToOneIds() {
        if (toOneIds == null) {
            toOneIds = new HashMap<>();
        }

        return toOneIds;
    }

    private Map<String, Set<Object>> mutableToManyIds() {
        if (toManyIds == null) {
            toManyIds = new HashMap<>();
        }

        return toManyIds;
    }
}
