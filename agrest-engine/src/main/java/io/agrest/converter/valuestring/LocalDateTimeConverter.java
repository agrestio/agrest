package io.agrest.converter.valuestring;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeConverter extends AbstractConverter<LocalDateTime> {

	private static final LocalDateTimeConverter instance = new LocalDateTimeConverter();

	public static LocalDateTimeConverter converter() {
		return instance;
	}

	private LocalDateTimeConverter() {
	}

	@Override
	protected String asStringNonNull(LocalDateTime dateTime) {
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
	}
}
