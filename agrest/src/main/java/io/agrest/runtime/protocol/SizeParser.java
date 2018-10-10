package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.protocol.Limit;
import io.agrest.protocol.Start;

import javax.ws.rs.core.Response;

/**
 * @since 2.13
 */
public class SizeParser implements ISizeParser {

    @Override
    public Start startFromJson(JsonNode json) {

        if (json != null) {
            if (!json.isNumber()) {
                throw new AgException(Response.Status.BAD_REQUEST, "Expected 'int' as 'start' value, got: " + json);
            }

            return new Start(json.asInt());
        }

        return null;
    }

    @Override
    public Limit limitFromJson(JsonNode json) {

        if (json != null) {
            if (!json.isNumber()) {
                throw new AgException(Response.Status.BAD_REQUEST, "Expected 'int' as 'limit' value, got: " + json);
            }

            return new Limit(json.asInt());
        }

        return null;
    }
}
