package com.nhl.link.rest.runtime.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.parser.converter.AbstractConverter;

import javax.ws.rs.core.Response;

public abstract class QueryParamConverter<T> extends AbstractConverter<T> {

    /**
     * Sanity check. We don't want to get a stack overflow.
     */
    public static void checkTooLong(String path) {
        if (path != null && path.length() > PathConstants.MAX_PATH_LENGTH) {
            throw new LinkRestException(Response.Status.BAD_REQUEST, "Include/exclude path too long: " + path);
        }
    }

    public abstract T fromString(String value);

    public abstract T fromRootNode(JsonNode root);
}
