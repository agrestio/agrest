package io.agrest.processor;

import io.agrest.SimpleResponse;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Key;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 1.16
 */
public abstract class BaseProcessingContext<T> implements ProcessingContext<T> {

    private Integer responseStatus;
    private Map<String, List<Object>> responseHeaders;
    private final Class<T> type;
    private final Injector injector;
    private Map<String, Object> properties;

    protected BaseProcessingContext(Class<T> type, Injector injector) {
        this.type = type;
        this.injector = injector;
    }

    /**
     * Provides access to a desired service from the Agrest stack that created the current context.
     *
     * @since 5.0
     */
    @Override
    public <S> S service(Class<S> type) {
        return injector.getInstance(type);
    }

    /**
     * Provides access to a desired service from the Agrest stack that created the current context.
     *
     * @since 5.0
     */
    @Override
    public <S> S service(Key<S> key) {
        return injector.getInstance(key);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public Object getProperty(String name) {
        return properties != null ? properties.get(name) : null;
    }

    @Override
    public void setProperty(String name, Object value) {
        mutableProperties().put(name, value);
    }

    /**
     * @since 5.0
     */
    public Integer getResponseStatus() {
        return responseStatus;
    }

    /**
     * @since 5.0
     */
    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    /**
     * @since 5.0
     */
    public Map<String, List<Object>> getResponseHeaders() {
        // header names are case-insensitive
        return responseHeaders != null ? responseHeaders : Collections.emptyMap();
    }

    /**
     * @since 5.0
     */
    public List<Object> getResponseHeaders(String name) {
        // header names are case-insensitive per RFC 2616
        return responseHeaders != null ? responseHeaders.get(name.toLowerCase()) : null;
    }

    /**
     * @since 5.0
     */
    public void addResponseHeader(String name, Object value) {
        // header names are case-insensitive per RFC 2616
        mutableResponseHeaders().computeIfAbsent(name.toLowerCase(), n -> new ArrayList<>()).add(value);
    }

    private Map<String, List<Object>> mutableResponseHeaders() {
        if (responseHeaders == null) {
            responseHeaders = new HashMap<>();
        }

        return responseHeaders;
    }

    private Map<String, Object> mutableProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }

        return properties;
    }
}
