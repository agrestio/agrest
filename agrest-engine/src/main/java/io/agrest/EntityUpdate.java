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

    private final Map<String, Object> idParts;
    private final Map<String, Object> attributes;
    private final Map<String, EntityUpdate<?>> toOnes;
    private final Map<String, List<EntityUpdate<?>>> toManys;
    private final Map<String, Set<Object>> relatedIds;

    private T targetObject;

    public EntityUpdate(AgEntity<T> entity) {
        this.entity = Objects.requireNonNull(entity);

        this.idParts = new HashMap<>();
        this.attributes = new HashMap<>();
        this.relatedIds = new HashMap<>();
        this.toManys = new HashMap<>();
        this.toOnes = new HashMap<>();
    }

    /**
     * Combines this update with another update. This update is modified with data of the other update.
     *
     * @return this update
     * @since 4.8
     */
    public EntityUpdate<T> merge(EntityUpdate<T> anotherUpdate) {

        this.attributes.putAll(anotherUpdate.attributes);
        this.toOnes.putAll(anotherUpdate.toOnes);
        this.toManys.putAll(anotherUpdate.toManys);
        this.relatedIds.putAll(anotherUpdate.relatedIds);

        if (!anotherUpdate.idParts.isEmpty()) {
            idParts.putAll(anotherUpdate.idParts);
        }

        // If we are merging a compatible update, "entity", "mergedTo" should all be identical already.
        // Do not override them.

        return this;
    }

    /**
     * @since 1.19
     */
    public AgEntity<T> getEntity() {
        return entity;
    }

    /**
     * @since 5.0
     */
    public void setIdParts(Map<String, Object> id) {
        idParts.putAll(id);
    }

    /**
     * @since 5.0
     */
    public void addIdPart(String idPart, Object value) {
        idParts.put(idPart, value);
    }

    /**
     * @since 5.0
     */
    public void addIdPartIfAbsent(String idPart, Object value) {
        idParts.putIfAbsent(idPart, value);
    }

    /**
     * @since 5.0
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Object getAttribute(String attribute) {
        return attributes.get(attribute);
    }

    /**
     * @since 5.0
     */
    public void setAttribute(String attribute, Object value) {
        attributes.put(attribute, value);
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
    public Map<String, List<EntityUpdate<?>>> getToManys() {
        return toManys;
    }

    /**
     * @since 5.0
     */
    public void addToMany(String relationshipName, EntityUpdate<?> update) {
        toManys.computeIfAbsent(relationshipName, n -> new ArrayList<>()).add(update);
    }

    /**
     * @since 5.0
     */
    public void setToOne(String relationshipName, EntityUpdate<?> update) {
        toOnes.put(relationshipName, update);
    }

    /**
     * @since 5.0
     */
    public <R> EntityUpdate<R> getToOne(String relationship) {
        return (EntityUpdate<R>) toOnes.get(relationship);
    }

    /**
     * @since 5.0
     */
    public Map<String, EntityUpdate<?>> getToOnes() {
        return toOnes;
    }

    /**
     * @since 5.0
     */
    public <R> List<EntityUpdate<R>> getToMany(String relationship) {
        List updates = toManys.get(relationship);
        return updates != null ? updates : Collections.emptyList();
    }

    /**
     * @since 5.0
     */
    public Map<String, Object> getIdParts() {
        return idParts;
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
}
