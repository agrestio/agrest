package io.agrest.converter.valuestring;


public abstract class AbstractConverter<T> implements ValueStringConverter<T> {

	@Override
	public String asString(T object) {
		if (object == null) {
			throw new NullPointerException("Null object");
		}
		
		return asStringNonNull(object);
	}

	protected abstract String asStringNonNull(T object);
}
