package io.agrest.converter.valuestring;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqlDateConverterTest {

    private final SqlDateConverter converter = SqlDateConverter.converter();

    @Test
    public void test() {
        assertEquals("2016-03-26", converter.asString(Date.valueOf(LocalDate.of(2016, 3, 26))));
    }
}
