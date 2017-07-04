package com.nhl.link.rest.processor;

import com.nhl.link.rest.SimpleResponse;

import javax.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.16
 */
public abstract class BaseProcessingContext<T> implements ProcessingContext<T> {

    private Class<T> type;
    private Map<String, Object> attributes;
    private Status status;

    public BaseProcessingContext(Class<T> type) {
        this.type = type;
    }

    /**
     * Returns a new SimpleResponse with status of this context.
     *
     * @return a new SimpleResponse with status of this context.
     * @since 1.24
     */
    public SimpleResponse createSimpleResponse() {
        SimpleResponse response = new SimpleResponse(true);
        response.setStatus(getStatus());
        return response;
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

        // presumably BaseProcessingContext is single-threaded, so lazy init and
        // using
        // like this is ok
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        attributes.put(name, value);
    }

    /**
     * @since 1.24
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @since 1.24
     */
    public void setStatus(Status status) {
        this.status = status;
    }
}
