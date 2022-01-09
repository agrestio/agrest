package io.agrest.jsonvalueconverter;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalTime;

public class ISOLocalTimeConverter extends AbstractConverter<LocalTime> {

    private static final ISOLocalTimeConverter instance = new ISOLocalTimeConverter();

    public static ISOLocalTimeConverter converter() {
        return instance;
    }

    @Override
    protected LocalTime valueNonNull(JsonNode node) {
        return LocalTime.parse(node.asText());
    }
}
