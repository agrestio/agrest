package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.time.Instant;
import java.util.Date;

public class ISODateConverter extends AbstractConverter {

	private static final ValueStringConverter instance = new ISODateConverter();

	public static ValueStringConverter converter() {
		return instance;
	}

	private ISODateConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return DateTimeFormatters.isoLocalDate().format(Instant.ofEpochMilli(date.getTime()));
	}
}
