package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.time.LocalDateTime;

public class LocalDateTimeConverter extends AbstractConverter<LocalDateTime> {

	private static final LocalDateTimeConverter instance = new LocalDateTimeConverter();

	public static LocalDateTimeConverter converter() {
		return instance;
	}

	private LocalDateTimeConverter() {
	}

	@Override
	protected String asStringNonNull(LocalDateTime dateTime) {
		return DateTimeFormatters.isoLocalDateTime().format(dateTime);
	}
}
