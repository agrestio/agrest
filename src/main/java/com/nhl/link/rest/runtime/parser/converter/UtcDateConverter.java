package com.nhl.link.rest.runtime.parser.converter;

import org.joda.time.DateTime;

public class UtcDateConverter implements ValueConverter {

	@Override
	public Object value(String stringValue) {
		return new DateTime(stringValue).toDate();
	}
}
