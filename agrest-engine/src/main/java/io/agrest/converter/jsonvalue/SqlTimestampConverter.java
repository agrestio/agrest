package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @since 5.0
 */
public class SqlTimestampConverter extends AbstractConverter<Timestamp> {

	private static final SqlTimestampConverter instance = new SqlTimestampConverter();

	public static SqlTimestampConverter converter() {
		return instance;
	}

	private SqlTimestampConverter() {}

	@Override
	protected Timestamp valueNonNull(JsonNode node) {
		LocalDateTime dateTime = LocalDateTime.parse(node.asText());
		return Timestamp.valueOf(dateTime);
	}
}
