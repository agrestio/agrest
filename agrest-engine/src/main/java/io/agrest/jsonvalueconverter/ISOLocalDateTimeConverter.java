package io.agrest.jsonvalueconverter;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class ISOLocalDateTimeConverter extends AbstractConverter<LocalDateTime> {

    private static final ISOLocalDateTimeConverter instance = new ISOLocalDateTimeConverter();

    public static ISOLocalDateTimeConverter converter() {
        return instance;
    }

    @Override
    protected LocalDateTime valueNonNull(JsonNode node) {
        return LocalDateTime.parse(node.asText());
    }
}
