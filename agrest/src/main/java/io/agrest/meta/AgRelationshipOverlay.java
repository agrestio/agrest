package io.agrest.meta;

import io.agrest.resolver.NestedDataResolver;

/**
 * @since 3.4
 */
public interface AgRelationshipOverlay {

    String getName();

    Class<?> getTargetType();

    boolean isToMany();

    NestedDataResolver<?> getResolver();

    default AgRelationship resolve(AgDataMap agDataMap) {
        AgEntity<?> targetEntity = agDataMap.getEntity(getTargetType());
        return new DefaultAgRelationship(getName(), targetEntity, isToMany(), getResolver());
    }
}
