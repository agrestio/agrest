package io.agrest.converter.valuestring;

import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqlTimeConverterTest {

    private final SqlTimeConverter converter = SqlTimeConverter.converter();

    @Test
    public void test() {
        assertEquals("13:27:27", converter.asString(Time.valueOf(LocalTime.of(13, 27, 27))));
    }

    @Test
    public void test1ms() {
        // can't use Time.valueOf(LocalTime), as for some reason it ignores nanonseconds. Using a longer conversion
        Time time = new Time(LocalTime.of(13, 27, 27, 1_000_000)
                .atDate(LocalDate.EPOCH)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());
        assertEquals("13:27:27.001", converter.asString(time));
    }

    @Test
    public void test100ms() {
        // can't use Time.valueOf(LocalTime), as for some reason it ignores nanonseconds. Using a longer conversion
        Time time = new Time(LocalTime.of(13, 27, 27, 100_000_000)
                .atDate(LocalDate.EPOCH)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());
        assertEquals("13:27:27.1", converter.asString(time));
    }

}
