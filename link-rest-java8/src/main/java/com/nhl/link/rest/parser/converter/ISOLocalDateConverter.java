package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;

public class ISOLocalDateConverter extends AbstractConverter {

    private static final JsonValueConverter instance = new ISOLocalDateConverter();

    public static JsonValueConverter converter() {
        return instance;
    }

    @Override
    protected Object valueNonNull(JsonNode node) {
        return LocalDate.parse(node.asText());
    }
}
