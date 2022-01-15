package io.agrest.converter.valuejson;

import io.agrest.encoder.DateTimeFormatters;

import java.time.LocalDateTime;

public class ISOLocalDateTimeConverter extends AbstractConverter {

	private static final ValueJsonConverter instance = new ISOLocalDateTimeConverter();

	public static ValueJsonConverter converter() {
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
