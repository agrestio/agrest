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
     * Returns a previously stored context attribute or null if none was set for a given key.
     *
     * @since 5.0
     */
    Object getProperty(String name);

    /**
     * Allows to store an arbitrary attribute in the context during processing.
     *
     * @since 5.0
     */
    void setProperty(String name, Object value);

    /**
     * @deprecated in favor of {@link #getProperty(String)}
     */
    @Deprecated(since = "5.0", forRemoval = true)
    default Object getAttribute(String name) {
        return getProperty(name);
    }

    /**
     * @deprecated in favor of {@link #setProperty(String, Object)}
     */
    @Deprecated(since = "5.0", forRemoval = true)
    default void setAttribute(String name, Object value) {
        setProperty(name, value);
    }

    /**
     * Provides access to a desired service from the Agrest stack that created the current context.
     *
     * @since 5.0
     */
    <S> S service(Class<S> type);

    /**
     * Provides access to a desired service from the Agrest stack that created the current context.
     *
     * @since 5.0
     */
    <S> S service(Key<S> key);
}
