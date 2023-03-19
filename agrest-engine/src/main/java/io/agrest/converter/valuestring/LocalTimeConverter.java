package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.time.LocalTime;

public class LocalTimeConverter extends AbstractConverter<LocalTime> {

	private static final LocalTimeConverter instance = new LocalTimeConverter();

	public static LocalTimeConverter converter() {
		return instance;
	}

	private LocalTimeConverter() {
	}

	@Override
	protected String asStringNonNull(LocalTime time) {
		return DateTimeFormatters.isoLocalTime().format(time);
	}
}
