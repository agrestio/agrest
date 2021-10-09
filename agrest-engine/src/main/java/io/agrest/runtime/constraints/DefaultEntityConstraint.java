package io.agrest.runtime.constraints;

import io.agrest.EntityConstraint;

import java.util.Set;

/**
 * @since 1.6
 */
class DefaultEntityConstraint implements EntityConstraint {

    private final String entityName;
    private final boolean allowsId;
    private final boolean allowsAllAttributes;
    private final Set<String> attributes;
    private final Set<String> relationships;

    DefaultEntityConstraint(
            String entityName,
            boolean allowsId,
            boolean allowsAllAttributes,
            Set<String> attributes,
            Set<String> relationships) {

        this.entityName = entityName;
        this.allowsId = allowsId;
        this.attributes = attributes;
        this.relationships = relationships;
        this.allowsAllAttributes = allowsAllAttributes;
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    @Override
    public boolean allowsId() {
        return allowsId;
    }

    @Override
    public boolean allowsAllAttributes() {
        return allowsAllAttributes;
    }

    @Override
    public boolean allowsAttribute(String name) {
        return attributes.contains(name);
    }

    @Override
    public boolean allowsRelationship(String name) {
        return relationships.contains(name);
    }

}
