package io.agrest;

/**
 * A base response object in Agrest.
 *
 * @since 1.19
 */
public abstract class AgResponse {

    protected final int status;

    public AgResponse(int status) {
        if (!HttpStatus.isValid(status)) {
            throw new IllegalArgumentException("Invalid HTTP status: " + status);
        }

        this.status = status;
    }

    /**
     * @since 4.7
     */
    public int getStatus() {
        return status;
    }
}
