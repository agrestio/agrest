package io.agrest.converter.valuejson;

import io.agrest.encoder.DateTimeFormatters;

import java.time.Instant;
import java.util.Date;

public class ISODateConverter extends AbstractConverter {

	private static final ValueJsonConverter instance = new ISODateConverter();

	public static ValueJsonConverter converter() {
		return instance;
	}

	private ISODateConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return DateTimeFormatters.isoLocalDate().format(Instant.ofEpochMilli(date.getTime()));
	}
}
