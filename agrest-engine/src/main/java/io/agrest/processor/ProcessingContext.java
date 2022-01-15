package io.agrest.processor;

import org.apache.cayenne.di.Key;

/**
 * An abstraction of a "context" object in a processor chain.
 *
 * @param <T> type of entity being processed.
 */
public interface ProcessingContext<T> {

    Class<T> getType();

    /**
     * Returns a previously stored context attribute or null if none was set for
     * a given key.
     */
    Object getAttribute(String name);

    /**
     * Allows to store an arbitrary attribute in the context during processing.
     */
    void setAttribute(String name, Object value);

    /**
     * Provides access to a desired service from the Agrest stack that created the current context.
     *
     * @since 5.0
     */
    <T> T service(Class<T> type);

    /**
     * Provides access to a desired service from the Agrest stack that created the current context.
     *
     * @since 5.0
     */
    <T> T service(Key<T> key);
}
