package io.agrest.converter.valuestring;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter extends AbstractConverter<LocalDate> {

	private static final LocalDateConverter instance = new LocalDateConverter();

	public static LocalDateConverter converter() {
		return instance;
	}

	private LocalDateConverter() {
	}

	@Override
	protected String asStringNonNull(LocalDate date) {
		return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
	}
}
