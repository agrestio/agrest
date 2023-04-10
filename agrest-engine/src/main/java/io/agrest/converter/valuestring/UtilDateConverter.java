package io.agrest.converter.valuestring;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class UtilDateConverter extends AbstractConverter<Date> {

	private static final UtilDateConverter instance = new UtilDateConverter();

	public static UtilDateConverter converter() {
		return instance;
	}

	private UtilDateConverter() {
	}

	@Override
	protected String asStringNonNull(Date date) {
		LocalDateTime ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ldt);
	}
}
