package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class ISOLocalDateTimeConverter extends AbstractConverter {

    private static final JsonValueConverter instance = new ISOLocalDateTimeConverter();

    public static JsonValueConverter converter() {
        return instance;
    }

    @Override
    protected Object valueNonNull(JsonNode node) {
        return LocalDateTime.parse(node.asText());
    }
}
