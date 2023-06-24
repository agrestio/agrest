package io.agrest.converter.valuestring;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalTimeConverterTest {

    private final LocalTimeConverter converter = LocalTimeConverter.converter();

    @Test
    public void asString() {
        assertEquals("13:27:27", converter.asString(LocalTime.of(13, 27, 27)));
    }

    @Test
    public void asString_1ms() {
        assertEquals("13:27:27.001", converter.asString(LocalTime.of(13, 27, 27, 1_000_000)));
    }

    @Test
    public void asString_100ms() {
        assertEquals("13:27:27.1", converter.asString(LocalTime.of(13, 27, 27, 100_000_000)));
    }
}
