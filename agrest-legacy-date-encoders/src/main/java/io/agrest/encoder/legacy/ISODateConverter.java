package io.agrest.encoder.legacy;

import io.agrest.encoder.converter.AbstractConverter;
import io.agrest.encoder.converter.StringConverter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @since 2.11
 * @deprecated since 2.11 in favor of using new date encoding strategy (default in the core module)
 */
@Deprecated
public class ISODateConverter extends AbstractConverter {

	private static final StringConverter instance = new ISODateConverter();

	public static StringConverter converter() {
		return instance;
	}

	private DateTimeFormatter format;

	private ISODateConverter() {
		format = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault());
	}

	@Override
	protected String asStringNonNull(Object object) {
		Date date = (Date) object;
		return format.format(Instant.ofEpochMilli(date.getTime()));
	}
}