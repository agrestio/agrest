package io.agrest.converter.valuejson;

import static io.agrest.encoder.DateTimeFormatters.isoOffsetDateTime;

import java.time.OffsetDateTime;

public class ISOOffsetDateTimeConverter extends AbstractConverter {

	private static final ValueJsonConverter instance = new ISOOffsetDateTimeConverter();

	public static ValueJsonConverter converter() {
		return instance;
	}

	private ISOOffsetDateTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		OffsetDateTime time = (OffsetDateTime) object;
		return isoOffsetDateTime().format(time);
	}
}