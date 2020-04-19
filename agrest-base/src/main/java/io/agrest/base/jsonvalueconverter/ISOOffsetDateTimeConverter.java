package io.agrest.base.jsonvalueconverter;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.JsonNode;

public class ISOOffsetDateTimeConverter extends AbstractConverter<OffsetDateTime> {

    private static final ISOOffsetDateTimeConverter instance = new ISOOffsetDateTimeConverter();

    public static ISOOffsetDateTimeConverter converter() {
        return instance;
    }

    @Override
    protected OffsetDateTime valueNonNull(JsonNode node) {
        return OffsetDateTime.parse(node.asText());
    }
}
