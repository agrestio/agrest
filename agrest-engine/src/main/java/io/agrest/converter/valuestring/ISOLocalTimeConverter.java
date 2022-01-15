package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.time.LocalTime;

public class ISOLocalTimeConverter extends AbstractConverter {

	private static final ValueStringConverter instance = new ISOLocalTimeConverter();

	public static ValueStringConverter converter() {
		return instance;
	}

	private ISOLocalTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		LocalTime time = (LocalTime) object;
		return DateTimeFormatters.isoLocalTime().format(time);
	}
}
