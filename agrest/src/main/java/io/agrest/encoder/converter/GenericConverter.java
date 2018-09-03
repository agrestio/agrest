package io.agrest.encoder.converter;


public class GenericConverter extends AbstractConverter {

	private static final StringConverter instance = new GenericConverter();

	public static StringConverter converter() {
		return instance;
	}

	private GenericConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		return object.toString();
	}
}
