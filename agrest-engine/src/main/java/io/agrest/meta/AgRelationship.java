package io.agrest.meta;

import io.agrest.resolver.NestedDataResolver;

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
     * @return a default data resolver for the target entity of this relationship
     * @since 3.4
     */
    NestedDataResolver<?> getResolver();
}
