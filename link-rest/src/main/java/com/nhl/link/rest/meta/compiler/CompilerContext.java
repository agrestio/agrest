package com.nhl.link.rest.meta.compiler;

import com.nhl.link.rest.meta.LrEntity;

/**
 * @since 2.0
 */
public interface CompilerContext {

    /**
     * Adds entity if it's not present in the lookup table yet;
     * returns either the new entity or the existing one
     * @since 2.0
     */
    <T> LrEntity<T> addEntityIfAbsent(Class<T> type, LrEntity<T> entity);

    /**
     * @since 2.0
     */
    <T> LrEntity<T> getOrCreateEntity(Class<T> type);
}
