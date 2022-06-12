package io.agrest;

import io.agrest.protocol.MessageResponse;

/**
 * Default implementation of {@link MessageResponse}.
 */
public class SimpleResponse extends AgResponse implements MessageResponse {

    protected final String message;

    /**
     * @since 5.0
     */
    public static SimpleResponse of(int status) {
        return new SimpleResponse(status, null);
    }

    /**
     * @since 5.0
     */
    public static SimpleResponse of(int status, String message) {
        return new SimpleResponse(status, message);
    }

    protected SimpleResponse(int status, String message) {
        super(status);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
