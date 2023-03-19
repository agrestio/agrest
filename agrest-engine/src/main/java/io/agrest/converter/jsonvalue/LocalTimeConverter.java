package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalTime;

public class LocalTimeConverter extends AbstractConverter<LocalTime> {

    private static final LocalTimeConverter instance = new LocalTimeConverter();

    public static LocalTimeConverter converter() {
        return instance;
    }

    @Override
    protected LocalTime valueNonNull(JsonNode node) {
        return LocalTime.parse(node.asText());
    }
}
