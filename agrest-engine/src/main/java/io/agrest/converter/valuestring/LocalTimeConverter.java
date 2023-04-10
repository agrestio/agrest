package io.agrest.converter.valuestring;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeConverter extends AbstractConverter<LocalTime> {

	private static final LocalTimeConverter instance = new LocalTimeConverter();

	public static LocalTimeConverter converter() {
		return instance;
	}

	private LocalTimeConverter() {
	}

	@Override
	protected String asStringNonNull(LocalTime time) {
		return DateTimeFormatter.ISO_LOCAL_TIME.format(time);
	}
}
