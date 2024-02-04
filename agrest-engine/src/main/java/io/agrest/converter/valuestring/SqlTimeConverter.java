package io.agrest.converter.valuestring;

import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SqlTimeConverter extends AbstractConverter<Time> {

	private static final SqlTimeConverter instance = new SqlTimeConverter();

	public static SqlTimeConverter converter() {
		return instance;
	}

	private SqlTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Time time) {
		// can't use Time.toLocalTime() as it loses milliseconds
		LocalTime lt = LocalTime.ofInstant(Instant.ofEpochMilli(time.getTime()), ZoneId.systemDefault());
		return DateTimeFormatter.ISO_LOCAL_TIME.format(lt);
	}
}
