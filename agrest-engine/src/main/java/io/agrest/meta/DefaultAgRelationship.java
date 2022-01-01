package io.agrest.meta;

import io.agrest.resolver.NestedDataResolver;

import java.util.Objects;

/**
 * @since 1.12
 */
public class DefaultAgRelationship implements AgRelationship {

    private String name;
    private AgEntity<?> targetEntity;
    private boolean toMany;
    private final boolean readable;
    private final boolean writable;
    private NestedDataResolver<?> dataResolver;

    public DefaultAgRelationship(
            String name,
            AgEntity<?> targetEntity,
            boolean toMany,
            boolean readable,
            boolean writable,
            NestedDataResolver<?> dataResolver) {

        this.name = name;
        this.toMany = toMany;
        this.targetEntity = Objects.requireNonNull(targetEntity);
        this.readable = readable;
        this.writable = writable;
        this.dataResolver = Objects.requireNonNull(dataResolver);
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
     * @since 4.7
     */
    @Override
    public boolean isReadable() {
        return readable;
    }

    /**
     * @since 4.7
     */
    @Override
    public boolean isWritable() {
        return writable;
    }

    @Override
    public NestedDataResolver<?> getResolver() {
        return dataResolver;
    }

    @Override
    public String toString() {
        String cardinality = toMany ? "to-many" : "to-one";
        return "DefaultAgRelationship[" + getName() + ", " + cardinality + "]";
    }
}
