package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;

public class ISOLocalDateConverter extends AbstractConverter<LocalDate> {

    private static final ISOLocalDateConverter instance = new ISOLocalDateConverter();

    public static ISOLocalDateConverter converter() {
        return instance;
    }

    @Override
    protected LocalDate valueNonNull(JsonNode node) {
        return LocalDate.parse(node.asText());
    }
}
