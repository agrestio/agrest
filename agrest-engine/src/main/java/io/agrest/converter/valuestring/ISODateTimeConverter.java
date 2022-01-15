package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.time.Instant;
import java.util.Date;

public class ISODateTimeConverter extends AbstractConverter {

	private static final ValueStringConverter instance = new ISODateTimeConverter();

	public static ValueStringConverter converter() {
		return instance;
	}

	private ISODateTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(date.getTime()));
	}
}
