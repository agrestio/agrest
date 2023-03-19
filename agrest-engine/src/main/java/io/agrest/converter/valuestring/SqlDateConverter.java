package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.sql.Date;
import java.time.Instant;

public class SqlDateConverter extends AbstractConverter<Date> {

	private static final SqlDateConverter instance = new SqlDateConverter();

	public static SqlDateConverter converter() {
		return instance;
	}

	private SqlDateConverter() {
	}

	@Override
	protected String asStringNonNull(Date date) {
		return DateTimeFormatters.isoLocalDate().format(Instant.ofEpochMilli(date.getTime()));
	}
}
