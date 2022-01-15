package io.agrest.converter.valuejson;

import io.agrest.encoder.DateTimeFormatters;

import java.time.Instant;
import java.util.Date;

public class ISODateTimeConverter extends AbstractConverter {

	private static final ValueJsonConverter instance = new ISODateTimeConverter();

	public static ValueJsonConverter converter() {
		return instance;
	}

	private ISODateTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(date.getTime()));
	}
}
