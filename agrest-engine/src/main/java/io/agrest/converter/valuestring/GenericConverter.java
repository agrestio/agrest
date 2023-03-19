package io.agrest.converter.valuestring;


public class GenericConverter extends AbstractConverter<Object> {

	private static final GenericConverter instance = new GenericConverter();

	public static GenericConverter converter() {
		return instance;
	}

	private GenericConverter() {
	}

	@Override
	protected String asStringNonNull(Object object) {
		return object.toString();
	}
}
