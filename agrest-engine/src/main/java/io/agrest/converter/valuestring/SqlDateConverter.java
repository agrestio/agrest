package io.agrest.converter.valuestring;

import java.sql.Date;
import java.time.format.DateTimeFormatter;

public class SqlDateConverter extends AbstractConverter<Date> {

	private static final SqlDateConverter instance = new SqlDateConverter();

	public static SqlDateConverter converter() {
		return instance;
	}

	private SqlDateConverter() {
	}

	@Override
	protected String asStringNonNull(Date date) {
		return DateTimeFormatter.ISO_LOCAL_DATE.format(date.toLocalDate());
	}
}
