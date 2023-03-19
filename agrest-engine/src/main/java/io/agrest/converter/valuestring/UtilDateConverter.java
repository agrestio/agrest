package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.time.Instant;
import java.util.Date;

public class UtilDateConverter extends AbstractConverter<Date> {

	private static final UtilDateConverter instance = new UtilDateConverter();

	public static UtilDateConverter converter() {
		return instance;
	}

	private UtilDateConverter() {
	}

	@Override
	protected String asStringNonNull(Date date) {
		return DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(date.getTime()));
	}
}
