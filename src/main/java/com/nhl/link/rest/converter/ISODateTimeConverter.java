package com.nhl.link.rest.converter;

import java.util.Date;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class ISODateTimeConverter extends AbstractConverter {

	private static final StringConverter instance = new ISODateTimeConverter();

	public static StringConverter converter() {
		return instance;
	}

	private DateTimeFormatter format;

	private ISODateTimeConverter() {
		this.format = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return format.print(date.getTime());
	}
}
