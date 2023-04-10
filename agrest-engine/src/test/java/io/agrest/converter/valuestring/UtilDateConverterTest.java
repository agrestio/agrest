package io.agrest.converter.valuestring;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilDateConverterTest {

    private final UtilDateConverter converter = UtilDateConverter.converter();

    @Test
    public void test() {
        Date date = Date.from(LocalDateTime.of(2016, 3, 26, 13, 27, 27).atZone(ZoneId.systemDefault()).toInstant());
        assertEquals("2016-03-26T13:27:27", converter.asString(date));
    }

    @Test
    public void test1ms() {
        Date date = Date.from(LocalDateTime.of(2016, 3, 26, 13, 27, 27, 1_000_000).atZone(ZoneId.systemDefault()).toInstant());
        assertEquals("2016-03-26T13:27:27.001", converter.asString(date));
    }

    @Test
    public void test100ms() {
        Date date = Date.from(LocalDateTime.of(2016, 3, 26, 13, 27, 27, 100_000_000).atZone(ZoneId.systemDefault()).toInstant());
        assertEquals("2016-03-26T13:27:27.1", converter.asString(date));
    }
}
