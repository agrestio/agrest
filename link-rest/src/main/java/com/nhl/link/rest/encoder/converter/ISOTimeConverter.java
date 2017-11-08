package com.nhl.link.rest.encoder.converter;

import com.nhl.link.rest.encoder.DateTimeFormatters;

import java.time.Instant;
import java.util.Date;

import static com.nhl.link.rest.encoder.DateTimeFormatters.isoLocalTime;

public class ISOTimeConverter extends AbstractConverter {

	private static final StringConverter instance = new ISOTimeConverter();

	public static StringConverter converter() {
		return instance;
	}

	private ISOTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return isoLocalTime().format(Instant.ofEpochMilli(date.getTime()));
	}
}
