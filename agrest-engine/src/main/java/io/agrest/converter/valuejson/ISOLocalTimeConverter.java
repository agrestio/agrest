package io.agrest.converter.valuejson;

import io.agrest.encoder.DateTimeFormatters;

import java.time.LocalTime;

public class ISOLocalTimeConverter extends AbstractConverter {

	private static final ValueJsonConverter instance = new ISOLocalTimeConverter();

	public static ValueJsonConverter converter() {
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
