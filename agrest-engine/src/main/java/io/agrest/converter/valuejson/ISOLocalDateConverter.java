package io.agrest.converter.valuejson;

import io.agrest.encoder.DateTimeFormatters;

import java.time.LocalDate;

public class ISOLocalDateConverter extends AbstractConverter {

	private static final ValueJsonConverter instance = new ISOLocalDateConverter();

	public static ValueJsonConverter converter() {
		return instance;
	}

	private ISOLocalDateConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		LocalDate date = (LocalDate) object;
		return DateTimeFormatters.isoLocalDate().format(date);
	}
}
