package io.agrest.encoder;

import io.agrest.ResourceEntity;

/**
 * @since 3.4
 */
@FunctionalInterface
public interface EncoderFilterEntityCondition {

    boolean test(ResourceEntity<?> entity);
}
