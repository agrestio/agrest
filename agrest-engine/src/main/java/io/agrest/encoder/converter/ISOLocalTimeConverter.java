package io.agrest.encoder.converter;

import java.time.LocalTime;

import static io.agrest.encoder.DateTimeFormatters.isoLocalTime;

public class ISOLocalTimeConverter extends AbstractConverter {

	private static final StringConverter instance = new ISOLocalTimeConverter();

	public static StringConverter converter() {
		return instance;
	}

	private ISOLocalTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		LocalTime time = (LocalTime) object;
		return isoLocalTime().format(time);
	}
}
