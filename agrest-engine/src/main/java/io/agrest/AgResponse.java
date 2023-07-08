package io.agrest;

import java.util.List;
import java.util.Map;

/**
 * A base response object in Agrest.
 *
 * @since 1.19
 */
public abstract class AgResponse {

    protected final int status;
    protected final Map<String, List<Object>> headers;

    protected AgResponse(int status, Map<String, List<Object>> headers) {
        if (!HttpStatus.isValid(status)) {
            throw new IllegalArgumentException("Invalid HTTP status: " + status);
        }

        this.status = status;
        this.headers = headers;
    }

    /**
     * @since 4.7
     */
    public int getStatus() {
        return status;
    }

    /**
     * @since 5.0
     */
    public Map<String, List<Object>> getHeaders() {
        return headers;
    }
}
