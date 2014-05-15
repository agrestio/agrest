package com.nhl.link.rest.encoder;

import java.io.IOException;
import java.util.Date;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.core.JsonGenerator;

public class ISODateTimeEncoder extends AbstractEncoder {

	private static final Encoder instance = new ISODateTimeEncoder();

	public static Encoder encoder() {
		return instance;
	}

	private DateTimeFormatter format;

	private ISODateTimeEncoder() {
		this.format = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
		Date date = (Date) object;
		String formatted = format.print(date.getTime());
		out.writeObject(formatted);
		return true;
	}
}
