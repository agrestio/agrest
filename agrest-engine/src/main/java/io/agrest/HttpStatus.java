package io.agrest;

/**
 * A collection of standard HTTP response statuses.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">HTTP response status codes</a>
 * @since 4.7
 */
public final class HttpStatus {

    private HttpStatus() {
    }

    /**
     * @since 5.0
     */
    public static boolean isValid(int status) {
        return status >= 100 && status <= 599;
    }

    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;
    public static final int NO_CONTENT = 204;
    public static final int RESET_CONTENT = 205;
    public static final int PARTIAL_CONTENT = 206;

    public static final int MOVED_PERMANENTLY = 301;
    public static final int FOUND = 302;
    public static final int SEE_OTHER = 303;
    public static final int NOT_MODIFIED = 304;
    public static final int USE_PROXY = 305;
    public static final int TEMPORARY_REDIRECT = 307;

    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int PAYMENT_REQUIRED = 402;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int REQUEST_TIMEOUT = 40;
    public static final int CONFLICT = 409;
    public static final int GONE = 410;
    public static final int LENGTH_REQUIRED = 411;
    public static final int PRECONDITION_FAILED = 412;
    public static final int REQUEST_ENTITY_TOO_LARGE = 413;
    public static final int REQUEST_URI_TOO_LONG = 414;
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    public static final int EXPECTATION_FAILED = 417;
    public static final int PRECONDITION_REQUIRED = 428;
    public static final int TOO_MANY_REQUESTS = 429;
    public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;

    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int GATEWAY_TIMEOUT = 504;
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;
    public static final int NETWORK_AUTHENTICATION_REQUIRED = 511;
}
