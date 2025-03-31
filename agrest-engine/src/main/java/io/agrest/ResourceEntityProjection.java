package io.agrest;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Stores attributes and relationships of a ResourceEntity for a single AgEntity. When inheritance is in play,
 * root entity and each sub-entity will have its own "projection".
 *
 * @since 5.0
 */
public class ResourceEntityProjection<T> {

    private final AgEntity<T> agEntity;
    private final Map<String, AgAttribute> attributes;
    private final Set<String> defaultAttributes;
    private final Map<String, AgRelationship> relationships;

    public ResourceEntityProjection(AgEntity<T> agEntity) {
        this.agEntity = agEntity;
        this.attributes = new HashMap<>();
        this.defaultAttributes = new HashSet<>();
        this.relationships = new HashMap<>();
    }

    public AgEntity<T> getAgEntity() {
        return agEntity;
    }

    public Collection<AgAttribute> getAttributes() {
        return attributes.values();
    }

    public AgAttribute getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * @return true if the attribute was added to the projection or was already a part of the projection
     */
    public boolean ensureAttribute(String name, boolean isDefault) {
        AgAttribute projectionAttribute = agEntity.getAttribute(name);
        if (projectionAttribute != null) {
            attributes.put(name, projectionAttribute);

            if (isDefault) {
                defaultAttributes.add(name);
            }

            return true;
        }

        return false;
    }

    public boolean removeAttribute(String name) {
        AgAttribute removed = attributes.remove(name);
        if (removed != null) {
            defaultAttributes.remove(name);
            return true;
        }

        return false;
    }

    public boolean isDefaultAttribute(String name) {
        return defaultAttributes.contains(name);
    }

    public Collection<AgRelationship> getRelationships() {
        return relationships.values();
    }

    public AgRelationship getRelationship(String name) {
        return relationships.get(name);
    }

    public boolean ensureRelationship(String name) {
        AgRelationship projectionRelationship = agEntity.getRelationship(name);
        if (projectionRelationship != null) {
            relationships.put(name, projectionRelationship);
            return true;
        }

        return false;
    }

    public boolean removeRelationship(String name) {
        return relationships.remove(name) != null;
    }
}
