package io.agrest.converter.valuejson;

import java.time.LocalTime;

import static io.agrest.encoder.DateTimeFormatters.isoLocalTime;

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
		return isoLocalTime().format(time);
	}
}
