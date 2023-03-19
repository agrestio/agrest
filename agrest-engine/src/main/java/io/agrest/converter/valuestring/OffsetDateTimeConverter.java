package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.time.OffsetDateTime;

public class OffsetDateTimeConverter extends AbstractConverter<OffsetDateTime> {

    private static final OffsetDateTimeConverter instance = new OffsetDateTimeConverter();

    public static OffsetDateTimeConverter converter() {
        return instance;
    }

    private OffsetDateTimeConverter() {
    }

    @Override
    protected String asStringNonNull(OffsetDateTime time) {
        return DateTimeFormatters.isoOffsetDateTime().format(time);
    }
}