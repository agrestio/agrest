package io.agrest.meta;

import io.agrest.property.PropertyReader;

import java.util.Objects;

/**
 * @since 1.12
 */
public class DefaultAgRelationship implements AgRelationship {

    private String name;
    private AgEntity<?> targetEntity;
    private boolean toMany;
    private PropertyReader propertyReader;

    /**
     * @since 2.10
     */
    public DefaultAgRelationship(String name, AgEntity<?> targetEntity, boolean toMany, PropertyReader propertyReader) {
        this.name = name;
        this.targetEntity = Objects.requireNonNull(targetEntity);
        this.toMany = toMany;
        this.propertyReader = propertyReader;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AgEntity<?> getTargetEntity() {
        return targetEntity;
    }

    @Override
    public boolean isToMany() {
        return toMany;
    }

    /**
     * @since 2.10
     */
    @Override
    public PropertyReader getPropertyReader() {
        return propertyReader;
    }
}
