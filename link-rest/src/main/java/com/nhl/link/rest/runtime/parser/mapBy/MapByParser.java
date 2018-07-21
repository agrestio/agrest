package com.nhl.link.rest.runtime.parser.mapBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.protocol.MapBy;

import javax.ws.rs.core.Response;

/**
 * @since 2.13
 */
public class MapByParser implements IMapByParser {

    @Override
    public MapBy fromString(String value) {
        return new MapBy(value);
    }

    @Override
    public MapBy fromJson(JsonNode json) {

        if (json != null) {

            if (!json.isTextual()) {
                throw new LinkRestException(Response.Status.BAD_REQUEST, "Expected textual value, got: " + json);
            }

            return fromString(json.asText());
        }

        return null;
    }
}
