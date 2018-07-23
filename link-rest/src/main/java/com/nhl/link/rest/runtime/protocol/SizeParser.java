package com.nhl.link.rest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.Start;

import javax.ws.rs.core.Response;

/**
 * @since 2.13
 */
public class SizeParser implements ISizeParser {

    @Override
    public Start startFromJson(JsonNode json) {

        if (json != null) {
            if (!json.isNumber()) {
                throw new LinkRestException(Response.Status.BAD_REQUEST, "Expected 'int' as 'start' value, got: " + json);
            }

            return new Start(json.asInt());
        }

        return null;
    }

    @Override
    public Limit limitFromJson(JsonNode json) {

        if (json != null) {
            if (!json.isNumber()) {
                throw new LinkRestException(Response.Status.BAD_REQUEST, "Expected 'int' as 'limit' value, got: " + json);
            }

            return new Limit(json.asInt());
        }

        return null;
    }
}
