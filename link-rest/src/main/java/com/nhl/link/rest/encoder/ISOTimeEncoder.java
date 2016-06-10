package com.nhl.link.rest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ISOTimeEncoder extends AbstractEncoder {

	private static final Encoder instance = new ISOTimeEncoder();

	public static Encoder encoder() {
		return instance;
	}

	private DateTimeFormatter format;

	private ISOTimeEncoder() {
		format = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
		Date date = (Date) object;
		String formatted = format.format(Instant.ofEpochMilli(date.getTime()));
		out.writeObject(formatted);
		return true;
	}
}
