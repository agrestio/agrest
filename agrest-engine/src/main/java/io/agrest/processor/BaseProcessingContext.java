package io.agrest.processor;

import io.agrest.SimpleResponse;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Key;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.16
 */
public abstract class BaseProcessingContext<T> implements ProcessingContext<T> {

    private final Class<T> type;
    private final Injector injector;
    private Map<String, Object> attributes;
    private int status;

    public BaseProcessingContext(Class<T> type, Injector injector) {
        this.type = type;
        this.injector = injector;
    }

    /**
     * Provides access to a desired service from the Agrest stack that created the current context.
     *
     * @since 5.0
     */
    @Override
    public <T> T service(Class<T> type) {
        return injector.getInstance(type);
    }

    /**
     * Provides access to a desired service from the Agrest stack that created the current context.
     *
     * @since 5.0
     */
    @Override
    public <T> T service(Key<T> key) {
        return injector.getInstance(key);
    }

    /**
     * Returns a new SimpleResponse with status of this context.
     *
     * @return a new SimpleResponse with status of this context.
     * @since 1.24
     */
    public SimpleResponse createSimpleResponse() {
        return SimpleResponse.of(getStatus());
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes != null ? attributes.get(name) : null;
    }

    @Override
    public void setAttribute(String name, Object value) {

        // Presumably BaseProcessingContext is single-threaded (one per request), so lazy init and using HashMap is ok.
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        attributes.put(name, value);
    }

    /**
     * @since 4.7
     */
    public int getStatus() {
        return status;
    }

    /**
     * @since 4.7
     */
    public void setStatus(int status) {
        this.status = status;
    }
}
