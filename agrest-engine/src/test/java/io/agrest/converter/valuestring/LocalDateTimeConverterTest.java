package io.agrest.converter.valuestring;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalDateTimeConverterTest {

    private final LocalDateTimeConverter converter = LocalDateTimeConverter.converter();

    @Test
    public void asString() {
        assertEquals("2016-03-26T13:27:27", converter.asString(LocalDateTime.of(2016, 3, 26, 13, 27, 27)));
    }

    @Test
    public void asString_distantPast() {
        assertEquals("1815-02-02T01:00:01", converter.asString(LocalDateTime.of(1815, 2, 2, 1, 0, 1)));
    }

    @Test
    public void asString_1ms() {
        assertEquals("2016-03-26T13:27:27.001",
                converter.asString(LocalDateTime.of(2016, 3, 26, 13, 27, 27, 1_000_000)));
    }

    @Test
    public void asString_100ms() {
        assertEquals("2016-03-26T13:27:27.1",
                converter.asString(LocalDateTime.of(2016, 3, 26, 13, 27, 27, 100_000_000)));
    }
}
