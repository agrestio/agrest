package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

public class ISODateEncoder extends AbstractEncoder {

	private static final Encoder instance = new ISODateEncoder();

	public static Encoder encoder() {
		return instance;
	}

	private ISODateEncoder() {
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
		Date date = (Date) object;
		String formatted = DateTimeFormatters.isoLocalDate().format(Instant.ofEpochMilli(date.getTime()));
		out.writeObject(formatted);
		return true;
	}
}
