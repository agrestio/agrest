package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

public class ISODateTimeEncoder extends AbstractEncoder {

	private static final Encoder instance = new ISODateTimeEncoder();

	public static Encoder encoder() {
		return instance;
	}

	private ISODateTimeEncoder() {
	}

	@Override
	protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
		Date date = (Date) object;
		String formatted = DateTimeFormatters.isoLocalDateTime().format(Instant.ofEpochMilli(date.getTime()));
		out.writeObject(formatted);
		return true;
	}
}
