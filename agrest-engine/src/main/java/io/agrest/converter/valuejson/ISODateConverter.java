package io.agrest.converter.valuejson;

import java.time.Instant;
import java.util.Date;

import static io.agrest.encoder.DateTimeFormatters.isoLocalDate;

public class ISODateConverter extends AbstractConverter {

	private static final ValueJsonConverter instance = new ISODateConverter();

	public static ValueJsonConverter converter() {
		return instance;
	}

	private ISODateConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return isoLocalDate().format(Instant.ofEpochMilli(date.getTime()));
	}
}
