package io.agrest;

/**
 * Encapsulates an Agrest error condition described by an HTTP status code. Used by the framework internally and can
 * also be used in application code. For convenience all the static factory methods treat message string as a template
 * compatible with {@link String#format(String, Object...)}, and take a vararg of message string parameters.
 *
 * @see HttpStatus
 */
public class AgException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;

    /**
     * @since 4.7
     */
    public static AgException of(int httpStatus, String message, Object... messageParams) {
        return new AgException(httpStatus, null, message, messageParams);
    }

    /**
     * @since 4.7
     */
    public static AgException of(int httpStatus, Throwable th, String message, Object... messageParams) {
        return new AgException(httpStatus, th, message, messageParams);
    }

    /**
     * @since 4.7
     */
    public static AgException badRequest() {
        return new AgException(HttpStatus.BAD_REQUEST, null, null, null);
    }

    /**
     * @since 4.7
     */
    public static AgException badRequest(String message, Object... messageParams) {
        return new AgException(HttpStatus.BAD_REQUEST, null, message, messageParams);
    }

    /**
     * @since 4.7
     */
    public static AgException badRequest(Throwable th, String message, Object... messageParams) {
        return new AgException(HttpStatus.BAD_REQUEST, th, message, messageParams);
    }

    /**
     * @since 4.7
     */
    public static AgException forbidden() {
        return new AgException(HttpStatus.FORBIDDEN, null, null, null);
    }

    /**
     * @since 4.7
     */
    public static AgException forbidden(String message, Object... messageParams) {
        return new AgException(HttpStatus.FORBIDDEN, null, message, messageParams);
    }

    /**
     * @since 4.7
     */
    public static AgException forbidden(Throwable th, String message, Object... messageParams) {
        return new AgException(HttpStatus.FORBIDDEN, th, message, messageParams);
    }



    /**
     * @since 4.7
     */
    public static AgException notFound() {
        return new AgException(HttpStatus.NOT_FOUND, null, null, null);
    }

    /**
     * @since 4.7
     */
    public static AgException notFound(String message, Object... messageParams) {
        return new AgException(HttpStatus.NOT_FOUND, null, message, messageParams);
    }

    /**
     * @since 4.7
     */
    public static AgException notFound(Throwable th, String message, Object... messageParams) {
        return new AgException(HttpStatus.NOT_FOUND, th, message, messageParams);
    }

    /**
     * @since 4.7
     */
    public static AgException internalServerError() {
        return new AgException(HttpStatus.INTERNAL_SERVER_ERROR, null, null, null);
    }

    /**
     * @since 4.7
     */
    public static AgException internalServerError(String message, Object... messageParams) {
        return new AgException(HttpStatus.INTERNAL_SERVER_ERROR, null, message, messageParams);
    }

    /**
     * @since 4.7
     */
    public static AgException internalServerError(Throwable th, String message, Object... messageParams) {
        return new AgException(HttpStatus.INTERNAL_SERVER_ERROR, th, message, messageParams);
    }

    protected AgException(int status, Throwable cause, String message, Object... messageParams) {
        super(messageParams != null && messageParams.length > 0 ? String.format(message, messageParams) : message, cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
