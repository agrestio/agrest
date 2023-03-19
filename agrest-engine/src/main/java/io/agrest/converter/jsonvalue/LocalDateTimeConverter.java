package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class LocalDateTimeConverter extends AbstractConverter<LocalDateTime> {

    private static final LocalDateTimeConverter instance = new LocalDateTimeConverter();

    public static LocalDateTimeConverter converter() {
        return instance;
    }

    @Override
    protected LocalDateTime valueNonNull(JsonNode node) {
        return LocalDateTime.parse(node.asText());
    }
}
