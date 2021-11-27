package io.agrest.encoder;

import io.agrest.filter.ReadFilter;

import java.util.Objects;

/**
 * A helper class to build EntityEncoderFilters. Usually created via static methods on {@link EntityEncoderFilter}.
 *
 * @since 3.4
 * @deprecated since 4.8 in favor of {@link ReadFilter}.
 */
@Deprecated
public class EntityEncoderFilterBuilder {

    private CompositeEntityEncoderFilter filter;

    public EntityEncoderFilterBuilder() {
        this.filter = new CompositeEntityEncoderFilter();
    }

    public EntityEncoderFilterBuilder forEntity(Class<?> entityType) {
        Objects.requireNonNull(entityType);
        filter.entityCondition = e -> entityType.equals(e.getType());
        return this;
    }

    public EntityEncoderFilterBuilder entityCondition(EncoderEntityCondition condition) {
        filter.entityCondition = Objects.requireNonNull(condition);
        return this;
    }

    public <T> EntityEncoderFilterBuilder objectCondition(EncoderObjectCondition<T> condition) {
        filter.objectCondition = Objects.requireNonNull(condition);
        return this;
    }

    public <T> EntityEncoderFilterBuilder encoder(EncoderMethod<T> encodeFunction) {
        filter.encoder = Objects.requireNonNull(encodeFunction);
        return this;
    }

    public EntityEncoderFilter build() {
        // make sure "filter" instance can't be mutated anymore outside this method
        CompositeEntityEncoderFilter filter = this.filter;
        this.filter = null;
        return filter;
    }
}
