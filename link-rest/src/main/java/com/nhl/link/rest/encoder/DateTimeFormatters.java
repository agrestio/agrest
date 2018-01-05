package com.nhl.link.rest.encoder;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;

/**
 * LR-specific date/time formatters
 *
 * @since 2.11
 */
public class DateTimeFormatters {

    /**
     * {@code HH:mm:ss[.SSS]}
     * Milliseconds are printed only if present in the temporal object
     */
    private static final DateTimeFormatter LR_ISO_LOCAL_TIME =
            new DateTimeFormatterBuilder()
                    .appendValue(HOUR_OF_DAY, 2)
                    .appendLiteral(':')
                    .appendValue(MINUTE_OF_HOUR, 2)
                    .optionalStart()
                    .appendLiteral(':')
                    .appendValue(SECOND_OF_MINUTE, 2)
                    .optionalStart()
                    .appendFraction(MILLI_OF_SECOND, 0, 3, true)
                    .toFormatter()
                    .withDecimalStyle(DecimalStyle.STANDARD)
                    .withZone(ZoneId.systemDefault());

    /**
     * {@code yyyy-MM-dd'T'HH:mm:ss[.SSS]}
     * Milliseconds are printed only if present in the temporal object
     */
    private static final DateTimeFormatter LR_ISO_LOCAL_DATE_TIME =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .append(DateTimeFormatter.ISO_LOCAL_DATE)
                    .appendLiteral('T')
                    .append(LR_ISO_LOCAL_TIME)
                    .toFormatter()
                    .withDecimalStyle(DecimalStyle.STANDARD)
                    .withZone(ZoneId.systemDefault());

    /**
     * {@code yyyy-MM-dd}
     */
    private static final DateTimeFormatter LR_ISO_LOCAL_DATE =
            DateTimeFormatter.ISO_LOCAL_DATE
                    .withZone(ZoneId.systemDefault());



    public static DateTimeFormatter isoLocalDateTime() {
        return LR_ISO_LOCAL_DATE_TIME;
    }

    public static DateTimeFormatter isoLocalDate() {
        return LR_ISO_LOCAL_DATE;
    }

    public static DateTimeFormatter isoLocalTime() {
        return LR_ISO_LOCAL_TIME;
    }
    
    public static DateTimeFormatter isoOffsetDateTime() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    }
}