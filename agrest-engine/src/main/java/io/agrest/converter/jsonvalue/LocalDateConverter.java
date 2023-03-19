package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;

public class LocalDateConverter extends AbstractConverter<LocalDate> {

    private static final LocalDateConverter instance = new LocalDateConverter();

    public static LocalDateConverter converter() {
        return instance;
    }

    @Override
    protected LocalDate valueNonNull(JsonNode node) {
        return LocalDate.parse(node.asText());
    }
}
