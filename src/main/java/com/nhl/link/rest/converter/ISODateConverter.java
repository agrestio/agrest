package com.nhl.link.rest.converter;

import java.util.Date;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class ISODateConverter extends AbstractConverter {

	private static final StringConverter instance = new ISODateConverter();

	public static StringConverter converter() {
		return instance;
	}

	private DateTimeFormatter format;

	private ISODateConverter() {
		this.format = ISODateTimeFormat.date();
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return format.print(date.getTime());
	}
}
