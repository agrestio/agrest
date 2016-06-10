package com.nhl.link.rest.encoder.converter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ISODateConverter extends AbstractConverter {

	private static final StringConverter instance = new ISODateConverter();

	public static StringConverter converter() {
		return instance;
	}

	private DateTimeFormatter format;

	private ISODateConverter() {
		format = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault());
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return format.format(Instant.ofEpochMilli(date.getTime()));
	}
}
