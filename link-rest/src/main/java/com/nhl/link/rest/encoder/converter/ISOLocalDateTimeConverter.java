package com.nhl.link.rest.encoder.converter;

import java.time.LocalDateTime;

import static com.nhl.link.rest.encoder.DateTimeFormatters.isoLocalDateTime;

public class ISOLocalDateTimeConverter extends AbstractConverter {

	private static final StringConverter instance = new ISOLocalDateTimeConverter();

	public static StringConverter converter() {
		return instance;
	}

	private ISOLocalDateTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		LocalDateTime dateTime = (LocalDateTime) object;
		return isoLocalDateTime().format(dateTime);
	}
}
