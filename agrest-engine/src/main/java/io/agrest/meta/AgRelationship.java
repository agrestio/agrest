package io.agrest.meta;

import io.agrest.resolver.RelatedDataResolver;

/**
 * @since 1.12
 */
public interface AgRelationship {

    String getName();

    /**
     * @since 2.0
     */
    AgEntity<?> getTargetEntity();

    boolean isToMany();

    /**
     * @since 4.7
     */
    boolean isReadable();

    /**
     * @since 4.7
     */
    boolean isWritable();

    /**
     * @return the data resolver for the target entity of this relationship
     * @since 5.0
     */
    RelatedDataResolver<?> getDataResolver();
}
