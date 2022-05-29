package io.agrest.protocol;

/**
 * Represents a "Message Response" document from the Agrest Protocol.
 *
 * @since 5.0
 */
public interface MessageResponse {

    /**
     * Returns an optional message returned from the server.
     */
    String getMessage();
}
