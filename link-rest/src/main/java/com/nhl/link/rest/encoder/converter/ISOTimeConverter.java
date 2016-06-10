package com.nhl.link.rest.encoder.converter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ISOTimeConverter extends AbstractConverter {

	private static final StringConverter instance = new ISOTimeConverter();

	public static StringConverter converter() {
		return instance;
	}

	private DateTimeFormatter format;

	private ISOTimeConverter() {
		format = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return format.format(Instant.ofEpochMilli(date.getTime()));
	}
}
