package io.agrest.meta;

import io.agrest.resolver.RelatedDataResolver;

import java.util.Objects;

/**
 * @since 5.0
 */
public class DefaultRelationship implements AgRelationship {

    private final String name;
    private final AgEntity<?> targetEntity;
    private final boolean toMany;
    private final boolean readable;
    private final boolean writable;
    private final RelatedDataResolver<?> dataResolver;

    public DefaultRelationship(
            String name,
            AgEntity<?> targetEntity,
            boolean toMany,
            boolean readable,
            boolean writable,
            RelatedDataResolver<?> dataResolver) {

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
    public RelatedDataResolver<?> getDataResolver() {
        return dataResolver;
    }

    @Override
    public String toString() {
        String cardinality = toMany ? "to-many" : "to-one";
        return "DefaultAgRelationship[" + getName() + ", " + cardinality + "]";
    }
}
