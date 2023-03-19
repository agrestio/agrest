package io.agrest.converter.valuestring;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class SqlTimestampConverter extends AbstractConverter<Timestamp> {

	private static final SqlTimestampConverter instance = new SqlTimestampConverter();

	public static SqlTimestampConverter converter() {
		return instance;
	}

	private SqlTimestampConverter() {
	}

	@Override
	protected String asStringNonNull(Timestamp timestamp) {
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(timestamp.toLocalDateTime());
	}
}
