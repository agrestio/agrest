package io.agrest.client;

/**
 * @since 2.0
 */
public class AgClientException extends RuntimeException {

    private static final long serialVersionUID = 8027409723345873322L;

    public AgClientException(String message) {
        super(message);
    }

    public AgClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
