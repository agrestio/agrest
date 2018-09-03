package io.agrest.meta;

import io.agrest.property.PropertyReader;

import java.util.Objects;

/**
 * @since 1.12
 */
public class DefaultLrRelationship implements LrRelationship {

    private String name;
    private LrEntity<?> targetEntity;
    private boolean toMany;
    private PropertyReader propertyReader;

    public DefaultLrRelationship(String name, LrEntity<?> targetEntity, boolean toMany) {
        this(name, targetEntity, toMany, null);
    }

    /**
     * @since 2.10
     */
    public DefaultLrRelationship(String name, LrEntity<?> targetEntity, boolean toMany, PropertyReader propertyReader) {
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
    public LrEntity<?> getTargetEntity() {
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
