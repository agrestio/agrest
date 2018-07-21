package com.nhl.link.rest.runtime.parser.mapBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.protocol.MapBy;

import javax.ws.rs.core.Response;

public class MapByParser implements IMapByParser {

    @Override
    public MapBy fromString(String value) {

        return new MapBy(value);
    }

    @Override
    public MapBy fromRootNode(JsonNode root) {
        JsonNode mapByNode = root.get(MapBy.MAP_BY);

        if (mapByNode != null) {
            if (!mapByNode.isTextual()) {
                throw new LinkRestException(Response.Status.BAD_REQUEST, "Expected textual value, got: " + mapByNode.asText());
            }

            return fromString(mapByNode.asText());
        }

        return null;
    }
}
