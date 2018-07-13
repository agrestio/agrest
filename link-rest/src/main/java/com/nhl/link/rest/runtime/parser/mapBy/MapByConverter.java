package com.nhl.link.rest.runtime.parser.mapBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.parser.QueryParamConverter;
import com.nhl.link.rest.runtime.query.MapBy;

import javax.ws.rs.core.Response;

public class MapByConverter extends QueryParamConverter<MapBy> {

    @Override
    protected MapBy valueNonNull(JsonNode node) {
        if (!node.isTextual()) {
            throw new LinkRestException(Response.Status.BAD_REQUEST, "Expected textual value, got: " + node.asText());
        }

        return fromString(node.asText());
    }

    @Override
    public MapBy fromString(String value) {

        return new MapBy(value);
    }

    @Override
    public MapBy fromRootNode(JsonNode root) {
        JsonNode mapByNode = root.get(MapBy.getName());

        if (mapByNode != null) {
            return valueNonNull(mapByNode);
        }

        return null;
    }
}
