package com.nhl.link.rest.encoder;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DecimalStyle;

public class DateTimeFormatters {

    public static DateTimeFormatter isoLocalDateTime() {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME
                .withDecimalStyle(DecimalStyle.STANDARD)
                .withZone(ZoneId.systemDefault());
    }
}
