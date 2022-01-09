package io.agrest.converter.valuejson;

import java.time.LocalDateTime;

import static io.agrest.encoder.DateTimeFormatters.isoLocalDateTime;

public class ISOLocalDateTimeConverter extends AbstractConverter {

	private static final ValueJsonConverter instance = new ISOLocalDateTimeConverter();

	public static ValueJsonConverter converter() {
		return instance;
	}

	private ISOLocalDateTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		LocalDateTime dateTime = (LocalDateTime) object;
		return isoLocalDateTime().format(dateTime);
	}
}
