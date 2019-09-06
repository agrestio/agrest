package io.agrest.meta;

import io.agrest.property.IdReader;

import java.util.Collection;

/**
 * A model of an entity.
 *
 * @since 1.12
 */
public interface AgEntity<T> {

    String getName();

    Class<T> getType();

    Collection<AgAttribute> getIds();

    /**
     * @since 3.3
     */
    AgAttribute getId(String name);

    Collection<AgAttribute> getAttributes();

    AgAttribute getAttribute(String name);

    Collection<AgRelationship> getRelationships();

    AgRelationship getRelationship(String name);

    /**
     * @since 3.4
     */
    IdReader getIdReader();
}
