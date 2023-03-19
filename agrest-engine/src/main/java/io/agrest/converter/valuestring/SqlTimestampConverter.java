package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.sql.Timestamp;
import java.time.Instant;

public class SqlTimestampConverter extends AbstractConverter<Timestamp> {

	private static final SqlTimestampConverter instance = new SqlTimestampConverter();

	public static SqlTimestampConverter converter() {
		return instance;
	}

	private SqlTimestampConverter() {
	}

	@Override
	protected String asStringNonNull(Timestamp timestamp) {
		return DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(timestamp.getTime()));
	}
}
