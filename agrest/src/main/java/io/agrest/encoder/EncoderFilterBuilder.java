package io.agrest.encoder;

import java.util.Objects;

/**
 * A helper class to build EncoderFilters. Usually created via static methods on {@link EncoderFilter}.
 *
 * @since 3.4
 */
public class EncoderFilterBuilder {

    private CompositeEncoderFilter filter;

    public EncoderFilterBuilder() {
        this.filter = new CompositeEncoderFilter();
    }

    public EncoderFilterBuilder forEntity(Class<?> entityType) {
        Objects.requireNonNull(entityType);
        filter.entityCondition = e -> entityType.equals(e.getType());
        return this;
    }

    public EncoderFilterBuilder entityCondition(EncoderFilterEntityCondition condition) {
        filter.entityCondition = Objects.requireNonNull(condition);
        return this;
    }

    public <T> EncoderFilterBuilder objectCondition(EncoderFilterObjectCondition<T> condition) {
        filter.objectCondition = Objects.requireNonNull(condition);
        return this;
    }

    public <T> EncoderFilterBuilder encoder(EncoderFilterEncoder<T> encodeFunction) {
        filter.encoder = Objects.requireNonNull(encodeFunction);
        return this;
    }

    public EncoderFilter build() {
        // make sure "filter" instance can't be mutated anymore outside this method
        CompositeEncoderFilter filter = this.filter;
        this.filter = null;
        return filter;
    }
}
