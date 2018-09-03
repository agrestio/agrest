package io.agrest.encoder.legacy;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.encoder.AbstractEncoder;
import io.agrest.encoder.Encoder;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @since 2.11
 * @deprecated since 2.11 in favor of using new date encoding strategy (default in the core module)
 */
@Deprecated
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

