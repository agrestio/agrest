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

    private Integer responseStatus;
    private final Class<T> type;
    private final Injector injector;
    private Map<String, Object> attributes;

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

    /**
     * Returns a new SimpleResponse with status of this context.
     *
     * @return a new SimpleResponse with status of this context.
     * @since 1.24
     * @deprecated unused anymore, as the context should not be creating a response. It is the responsibility of a
     * pipeline
     */
    @Deprecated(since = "5.0")
    public SimpleResponse createSimpleResponse() {
        return SimpleResponse.of(getResponseStatus());
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public Object getProperty(String name) {
        return attributes != null ? attributes.get(name) : null;
    }

    @Override
    public void setProperty(String name, Object value) {

        // Presumably BaseProcessingContext is single-threaded (one per request), so lazy init and using HashMap is ok.
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        attributes.put(name, value);
    }

    /**
     * @deprecated in favor of {@link #getResponseStatus()}
     */
    @Deprecated(since = "5.0")
    public int getStatus() {
        return getResponseStatus() != null ? getResponseStatus() : 0;
    }

    /**
     * @deprecated in favor of {@link #setResponseStatus(Integer)}
     */
    @Deprecated(since = "5.0")
    public void setStatus(int responseStatus) {
        setResponseStatus(responseStatus);
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
}
