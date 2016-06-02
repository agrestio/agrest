package com.nhl.link.rest.client;

public class LinkRestClientException extends RuntimeException {

    LinkRestClientException(String message) {
        super(message);
    }

    LinkRestClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
