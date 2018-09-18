package io.agrest.runtime.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;
import io.agrest.protocol.MapBy;

import javax.ws.rs.core.Response;

/**
 * @since 2.13
 */
public class MapByParser implements IMapByParser {

    @Override
    public MapBy fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return new MapBy(value);
    }

    @Override
    public MapBy fromJson(JsonNode json) {

        if (json != null) {

            if (!json.isTextual()) {
                throw new AgException(Response.Status.BAD_REQUEST, "Expected textual value, got: " + json);
            }

            return fromString(json.asText());
        }

        return null;
    }
}
