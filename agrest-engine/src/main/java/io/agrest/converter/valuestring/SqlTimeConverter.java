package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.sql.Time;
import java.time.Instant;

public class SqlTimeConverter extends AbstractConverter<Time> {

	private static final SqlTimeConverter instance = new SqlTimeConverter();

	public static SqlTimeConverter converter() {
		return instance;
	}

	private SqlTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Time time) {
		return DateTimeFormatters.isoLocalTime().format(Instant.ofEpochMilli(time.getTime()));
	}
}
