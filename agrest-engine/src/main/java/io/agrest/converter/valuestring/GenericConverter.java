package io.agrest.converter.valuestring;


public class GenericConverter extends AbstractConverter {

	private static final ValueStringConverter instance = new GenericConverter();

	public static ValueStringConverter converter() {
		return instance;
	}

	private GenericConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		return object.toString();
	}
}
