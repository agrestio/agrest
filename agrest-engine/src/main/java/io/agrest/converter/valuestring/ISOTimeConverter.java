package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.time.Instant;
import java.util.Date;

public class ISOTimeConverter extends AbstractConverter {

	private static final ValueStringConverter instance = new ISOTimeConverter();

	public static ValueStringConverter converter() {
		return instance;
	}

	private ISOTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return DateTimeFormatters.isoLocalTime().format(Instant.ofEpochMilli(date.getTime()));
	}
}
