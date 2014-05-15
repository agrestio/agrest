package com.nhl.link.rest.encoder;

import java.io.IOException;
import java.util.Date;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import com.fasterxml.jackson.core.JsonGenerator;

public class ISOTimeEncoder extends AbstractEncoder {

	private static final Encoder instance = new ISOTimeEncoder();

	public static Encoder encoder() {
		return instance;
	}

	private DateTimeFormatter format;

	private ISOTimeEncoder() {
		this.format = new DateTimeFormatterBuilder().appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2)
				.appendLiteral(':').appendSecondOfMinute(2).toFormatter();
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
		Date date = (Date) object;
		String formatted = format.print(date.getTime());
		out.writeObject(formatted);
		return true;
	}
}
