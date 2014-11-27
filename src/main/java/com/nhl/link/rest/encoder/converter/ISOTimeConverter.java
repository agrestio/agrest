package com.nhl.link.rest.encoder.converter;

import java.util.Date;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public class ISOTimeConverter extends AbstractConverter {

	private static final StringConverter instance = new ISOTimeConverter();

	public static StringConverter converter() {
		return instance;
	}

	private DateTimeFormatter format;

	private ISOTimeConverter() {
		this.format = new DateTimeFormatterBuilder().appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2)
				.appendLiteral(':').appendSecondOfMinute(2).toFormatter().withZoneUTC();
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return format.print(date.getTime());
	}
}
