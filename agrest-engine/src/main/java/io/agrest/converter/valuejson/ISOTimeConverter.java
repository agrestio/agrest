package io.agrest.converter.valuejson;

import java.time.Instant;
import java.util.Date;

import static io.agrest.encoder.DateTimeFormatters.isoLocalTime;

public class ISOTimeConverter extends AbstractConverter {

	private static final ValueJsonConverter instance = new ISOTimeConverter();

	public static ValueJsonConverter converter() {
		return instance;
	}

	private ISOTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return isoLocalTime().format(Instant.ofEpochMilli(date.getTime()));
	}
}
