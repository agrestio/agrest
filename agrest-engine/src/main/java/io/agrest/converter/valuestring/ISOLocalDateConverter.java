package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.time.LocalDate;

public class ISOLocalDateConverter extends AbstractConverter {

	private static final ValueStringConverter instance = new ISOLocalDateConverter();

	public static ValueStringConverter converter() {
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
