package io.agrest.converter.valuejson;

import io.agrest.encoder.DateTimeFormatters;

import java.time.Instant;
import java.util.Date;

public class ISOTimeConverter extends AbstractConverter {

	private static final ValueJsonConverter instance = new ISOTimeConverter();

	public static ValueJsonConverter converter() {
		return instance;
	}

	private ISOTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return DateTimeFormatters.isoLocalTime().format(Instant.ofEpochMilli(date.getTime()));
	}
}
