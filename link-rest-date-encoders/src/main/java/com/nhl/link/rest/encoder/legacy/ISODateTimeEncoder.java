package com.nhl.link.rest.encoder.legacy;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.encoder.AbstractEncoder;
import com.nhl.link.rest.encoder.Encoder;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ISODateTimeEncoder extends AbstractEncoder {

	private static final Encoder instance = new ISODateTimeEncoder();

	public static Encoder encoder() {
		return instance;
	}

	private DateTimeFormatter format;

	private ISODateTimeEncoder() {
		format = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
		Date date = (Date) object;
		String formatted = format.format(Instant.ofEpochMilli(date.getTime()));
		out.writeObject(formatted);
		return true;
	}
}

