package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import static io.agrest.encoder.DateTimeFormatters.isoLocalTime;

public class ISOTimeEncoder extends AbstractEncoder {

	private static final Encoder instance = new ISOTimeEncoder();

	public static Encoder encoder() {
		return instance;
	}

	private ISOTimeEncoder() {
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
		Date date = (Date) object;
		String formatted = isoLocalTime().format(Instant.ofEpochMilli(date.getTime()));
		out.writeObject(formatted);
		return true;
	}
}
