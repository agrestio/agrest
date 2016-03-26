package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalTime;

public class ISOLocalTimeConverter extends AbstractConverter {

    private static final JsonValueConverter instance = new ISOLocalTimeConverter();

    public static JsonValueConverter converter() {
        return instance;
    }

    @Override
    protected Object valueNonNull(JsonNode node) {
        return LocalTime.parse(node.asText());
    }
}
