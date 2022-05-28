package io.agrest.meta;

/**
 * A holder of all combined metadata for the Agrest runtime. Consists of a number of entities, represented by
 * {@link AgEntity}.
 *
 * @since 5.0
 */
public interface AgSchema {

    <T> AgEntity<T> getEntity(Class<T> type);
}
