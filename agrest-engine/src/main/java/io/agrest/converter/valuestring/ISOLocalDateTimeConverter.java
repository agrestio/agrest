package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.time.LocalDateTime;

public class ISOLocalDateTimeConverter extends AbstractConverter {

	private static final ValueStringConverter instance = new ISOLocalDateTimeConverter();

	public static ValueStringConverter converter() {
		return instance;
	}

	private ISOLocalDateTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		LocalDateTime dateTime = (LocalDateTime) object;
		return DateTimeFormatters.isoLocalDateTime().format(dateTime);
	}
}
