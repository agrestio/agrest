package io.agrest.converter.valuestring;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalDateConverterTest {

    private final LocalDateConverter converter = LocalDateConverter.converter();

    @Test
    public void test() {
        assertEquals("2016-03-26", converter.asString(LocalDate.of(2016, 3, 26)));
    }
}
