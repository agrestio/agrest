package com.nhl.link.rest.encoder.converter;


public abstract class AbstractConverter implements StringConverter {

	@Override
	public String asString(Object object) {
		if (object == null) {
			throw new NullPointerException("Null object");
		}
		
		return asStringNonNull(object);
	}

	protected abstract String asStringNonNull(Object object);
}
