package io.agrest.converter.valuestring;


public abstract class AbstractConverter implements ValueStringConverter {

	@Override
	public String asString(Object object) {
		if (object == null) {
			throw new NullPointerException("Null object");
		}
		
		return asStringNonNull(object);
	}

	protected abstract String asStringNonNull(Object object);
}
