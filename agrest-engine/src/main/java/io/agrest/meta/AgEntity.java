package io.agrest.meta;

import io.agrest.property.IdReader;
import io.agrest.resolver.RootDataResolver;

import java.util.Collection;

/**
 * A model of an entity.
 *
 * @since 1.12
 */
public interface AgEntity<T> {

    /**
     * @return a mutable overlay object that can be used to customize the entity.
     * @since 3.4
     */
    static <T> AgEntityOverlay<T> overlay(Class<T> type) {
        return new AgEntityOverlay<>(type);
    }

    String getName();

    Class<T> getType();

    Collection<AgAttribute> getIds();

    /**
     * @since 3.4
     */
    AgAttribute getIdAttribute(String name);

    Collection<AgAttribute> getAttributes();

    AgAttribute getAttribute(String name);

    Collection<AgRelationship> getRelationships();

    AgRelationship getRelationship(String name);

    /**
     * @since 3.4
     */
    IdReader getIdReader();

    /**
     * @return a default data resolver for this entity for when it is resolved as a root of a request.
     * @since 3.4
     */
    RootDataResolver<T> getDataResolver();
}
