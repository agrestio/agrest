package io.agrest.base.jsonvalueconverter;

import com.fasterxml.jackson.databind.node.TextNode;
import io.agrest.base.jsonvalueconverter.UtcDateConverter;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static org.junit.Assert.assertEquals;

public class UtcDateConverterTest {

    // copied from DataTimeFormatters class that is not accessible here...
    private static final DateTimeFormatter AGREST_ISO_LOCAL_TIME =
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

    private static final DateTimeFormatter AGREST_ISO_LOCAL_DATE_TIME =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .append(DateTimeFormatter.ISO_LOCAL_DATE)
                    .appendLiteral('T')
                    .append(AGREST_ISO_LOCAL_TIME)
                    .toFormatter()
                    .withDecimalStyle(DecimalStyle.STANDARD)
                    .withZone(ZoneId.systemDefault());

    private static String isoFormat(java.util.Date date) {
        return AGREST_ISO_LOCAL_DATE_TIME.format(Instant.ofEpochMilli(date.getTime()));
    }

    @SuppressWarnings("unchecked")
    private static <T extends java.util.Date> T convert(Class<T> targetType, String value) {
        return (T) UtcDateConverter.converter(targetType).value(new TextNode(value));
    }

    @Test
    public void testConverter_javaUtilDate() {
        java.util.Date date = new java.util.Date();
        java.util.Date parsed = convert(java.util.Date.class, isoFormat(date));
        assertEquals(date, parsed);
    }

    @Test
    public void testConverter_javaSqlDate() {
        java.util.Date date = new java.util.Date();
        java.sql.Date parsed = convert(java.sql.Date.class, isoFormat(date));
        assertEquals(date, parsed);
    }

    @Test
    public void testConverter_javaSqlTime() {
        java.util.Date date = new java.util.Date();
        java.sql.Time parsed = convert(java.sql.Time.class, isoFormat(date));
        assertEquals(date, parsed);
    }

    @Test
    public void testConverter_javaSqlTimestamp() {
        java.util.Date date = new java.util.Date();
        java.sql.Timestamp parsed = convert(java.sql.Timestamp.class, isoFormat(date));
        assertEquals(date, parsed);
    }
}
