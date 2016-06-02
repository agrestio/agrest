package com.nhl.link.rest.client.runtime.jackson;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 2.0
 */
public class JsonEntityReader implements IJsonEntityReader<JsonNode> {

    @Override
    public JsonNode readEntity(JsonNode node) {
        return node;
    }
}
