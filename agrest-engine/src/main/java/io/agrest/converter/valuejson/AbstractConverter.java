package io.agrest.converter.valuejson;


public abstract class AbstractConverter implements ValueJsonConverter {

	@Override
	public String asString(Object object) {
		if (object == null) {
			throw new NullPointerException("Null object");
		}
		
		return asStringNonNull(object);
	}

	protected abstract String asStringNonNull(Object object);
}
