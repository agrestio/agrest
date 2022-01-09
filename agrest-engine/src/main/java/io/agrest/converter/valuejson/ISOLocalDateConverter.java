package io.agrest.converter.valuejson;

import java.time.LocalDate;

import static io.agrest.encoder.DateTimeFormatters.isoLocalDate;

public class ISOLocalDateConverter extends AbstractConverter {

	private static final ValueJsonConverter instance = new ISOLocalDateConverter();

	public static ValueJsonConverter converter() {
		return instance;
	}

	private ISOLocalDateConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		LocalDate date = (LocalDate) object;
		return isoLocalDate().format(date);
	}
}
