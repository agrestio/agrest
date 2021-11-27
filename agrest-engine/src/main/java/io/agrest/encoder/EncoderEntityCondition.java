package io.agrest.encoder;

import io.agrest.ResourceEntity;
import io.agrest.filter.ReadFilter;

/**
 * @since 3.4
 * @deprecated since 4.8 in favor of {@link ReadFilter}.
 */
@Deprecated
@FunctionalInterface
public interface EncoderEntityCondition {

    boolean test(ResourceEntity<?> entity);
}
