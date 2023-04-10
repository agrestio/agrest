package io.agrest.converter.valuestring;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeConverter extends AbstractConverter<OffsetDateTime> {

    private static final OffsetDateTimeConverter instance = new OffsetDateTimeConverter();

    public static OffsetDateTimeConverter converter() {
        return instance;
    }

    private OffsetDateTimeConverter() {
    }

    @Override
    protected String asStringNonNull(OffsetDateTime time) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(time);
    }
}