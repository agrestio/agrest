package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @since 5.0
 */
public class UtilDateConverter extends AbstractConverter<Date> {

	private static final UtilDateConverter instance = new UtilDateConverter();

	public static UtilDateConverter converter() {
		return instance;
	}

	private UtilDateConverter() {}

	@Override
	protected Date valueNonNull(JsonNode node) {
		LocalDateTime dateTime = LocalDateTime.parse(node.asText());
		return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
}
