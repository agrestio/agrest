package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;

import java.sql.Date;
import java.time.LocalDate;

/**
 * @since 5.0
 */
public class SqlDateConverter extends AbstractConverter<Date> {

	private static final SqlDateConverter instance = new SqlDateConverter();

	public static SqlDateConverter converter() {
		return instance;
	}

	private SqlDateConverter() {}

	@Override
	protected Date valueNonNull(JsonNode node) {
		LocalDate date = LocalDate.parse(node.asText());
		return Date.valueOf(date);
	}
}
