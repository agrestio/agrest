package io.agrest.meta;

import io.agrest.ResourceEntity;
import io.agrest.property.PropertyReader;

import java.util.Objects;
import java.util.function.Function;

/**
 * @since 1.12
 */
public class DefaultAgRelationship implements AgRelationship {

    private String name;
    private AgEntity<?> targetEntity;
    private boolean toMany;
    private Function<ResourceEntity<?>, PropertyReader> readerFactory;

    /**
     * @since 2.10
     */
    public DefaultAgRelationship(
            String name, AgEntity<?> targetEntity,
            boolean toMany,
            Function<ResourceEntity<?>, PropertyReader> readerFactory) {
        this.name = name;
        this.targetEntity = Objects.requireNonNull(targetEntity);
        this.toMany = toMany;
        this.readerFactory = readerFactory;
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
    public PropertyReader getPropertyReader(ResourceEntity<?> entity) {
        return readerFactory.apply(entity);
    }
}
