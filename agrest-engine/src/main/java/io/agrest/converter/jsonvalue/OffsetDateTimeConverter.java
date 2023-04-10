package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

public class OffsetDateTimeConverter extends AbstractConverter<OffsetDateTime> {

    private static final OffsetDateTimeConverter instance = new OffsetDateTimeConverter();

    public static OffsetDateTimeConverter converter() {
        return instance;
    }

    @Override
    protected OffsetDateTime valueNonNull(JsonNode node) {
        return OffsetDateTime.parse(node.asText());
    }
}
