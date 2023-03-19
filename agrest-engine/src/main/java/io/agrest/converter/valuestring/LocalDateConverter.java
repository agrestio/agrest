package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.time.LocalDate;

public class LocalDateConverter extends AbstractConverter<LocalDate> {

	private static final LocalDateConverter instance = new LocalDateConverter();

	public static LocalDateConverter converter() {
		return instance;
	}

	private LocalDateConverter() {
	}

	@Override
	protected String asStringNonNull(LocalDate date) {
		return DateTimeFormatters.isoLocalDate().format(date);
	}
}
