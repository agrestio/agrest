package com.nhl.link.rest.encoder.converter;

import java.time.LocalDate;

import static com.nhl.link.rest.encoder.DateTimeFormatters.isoLocalDate;

public class ISOLocalDateConverter extends AbstractConverter {

	private static final StringConverter instance = new ISOLocalDateConverter();

	public static StringConverter converter() {
		return instance;
	}

	private ISOLocalDateConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		LocalDate date = (LocalDate) object;
		return isoLocalDate().format(date);
	}
}
