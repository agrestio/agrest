package io.agrest.converter.valuejson;


public class GenericConverter extends AbstractConverter {

	private static final ValueJsonConverter instance = new GenericConverter();

	public static ValueJsonConverter converter() {
		return instance;
	}

	private GenericConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		return object.toString();
	}
}
