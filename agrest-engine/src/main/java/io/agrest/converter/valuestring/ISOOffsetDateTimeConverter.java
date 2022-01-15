package io.agrest.converter.valuestring;

import io.agrest.encoder.DateTimeFormatters;

import java.time.OffsetDateTime;

public class ISOOffsetDateTimeConverter extends AbstractConverter {

	private static final ValueStringConverter instance = new ISOOffsetDateTimeConverter();

	public static ValueStringConverter converter() {
		return instance;
	}

	private ISOOffsetDateTimeConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		OffsetDateTime time = (OffsetDateTime) object;
		return DateTimeFormatters.isoOffsetDateTime().format(time);
	}
}