package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * @since 5.0
 */
public class SqlTimeConverter extends AbstractConverter<Time> {

    private static final SqlTimeConverter instance = new SqlTimeConverter();

    public static SqlTimeConverter converter() {
        return instance;
    }

    private SqlTimeConverter() {
    }

    @Override
    protected Time valueNonNull(JsonNode node) {
        LocalTime time = LocalTime.parse(node.asText());

        // can't use Time.valueOf(LocalTime), as for some reason it ignores nanonseconds. Using a longer conversion
        return new Time(time
                .atDate(LocalDate.EPOCH)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());
    }
}
