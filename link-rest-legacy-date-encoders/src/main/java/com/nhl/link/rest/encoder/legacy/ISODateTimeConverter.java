package com.nhl.link.rest.encoder.legacy;

import com.nhl.link.rest.encoder.converter.AbstractConverter;
import com.nhl.link.rest.encoder.converter.StringConverter;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @since 2.11
 * @deprecated since 2.11 in favor of using new date encoding strategy (default in the core module)
 */
@Deprecated
public class ISODateTimeConverter extends AbstractConverter {

	private static final StringConverter instance = new ISODateTimeConverter();

	public static StringConverter converter() {
		return instance;
	}

	private DateTimeFormatter format;

	private ISODateTimeConverter() {
		format = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return format.format(Instant.ofEpochMilli(date.getTime()));
	}
}