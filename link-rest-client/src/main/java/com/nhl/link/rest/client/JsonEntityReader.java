package com.nhl.link.rest.client;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonEntityReader implements IJsonEntityReader<JsonNode> {

    @Override
    public JsonNode readEntity(JsonNode node) {
        return node;
    }
}
