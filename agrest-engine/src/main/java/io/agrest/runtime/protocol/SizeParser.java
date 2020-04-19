package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

import javax.ws.rs.core.Response;

/**
 * @since 2.13
 */
public class SizeParser implements ISizeParser {

    @Override
    public Integer startFromJson(JsonNode json) {

        if (json != null) {
            if (!json.isNumber()) {
                throw new AgException(Response.Status.BAD_REQUEST, "Expected 'int' as 'start' value, got: " + json);
            }

            return json.asInt();
        }

        return null;
    }

    @Override
    public Integer limitFromJson(JsonNode json) {

        if (json != null) {
            if (!json.isNumber()) {
                throw new AgException(Response.Status.BAD_REQUEST, "Expected 'int' as 'limit' value, got: " + json);
            }

            return json.asInt();
        }

        return null;
    }
}
