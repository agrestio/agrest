package io.agrest;

import io.agrest.protocol.MessageResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link MessageResponse}.
 */
public class SimpleResponse extends AgResponse implements MessageResponse {

    protected final String message;

    /**
     * @since 5.0
     */
    public static SimpleResponse of(int status) {
        return of(status, Collections.emptyMap(), null);
    }

    /**
     * @since 5.0
     */
    public static SimpleResponse of(int status, String message) {
        return of(status, Collections.emptyMap(), message);
    }

    /**
     * @since 5.0
     */
    public static SimpleResponse of(int status, Map<String, List<Object>> headers, String message) {
        return new SimpleResponse(
                status,
                headers != null ? headers : Collections.emptyMap(),
                message);
    }


    protected SimpleResponse(int status, Map<String, List<Object>> headers,  String message) {
        super(status, headers);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
