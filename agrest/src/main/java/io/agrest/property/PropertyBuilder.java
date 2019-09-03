package io.agrest.property;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.EntityProperty;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncoderVisitor;
import io.agrest.encoder.GenericEncoder;

import java.io.IOException;

/**
 * A {@link EntityProperty} implementation that provides fluent builder methods
 * for the manual property assembly.
 */
public class PropertyBuilder implements EntityProperty {

	private Encoder encoder;
	private PropertyReader reader;

	public static PropertyBuilder property() {
		return new PropertyBuilder(BeanPropertyReader.reader(), GenericEncoder.encoder());
	}

	public static PropertyBuilder property(PropertyReader reader) {
		return new PropertyBuilder(reader, GenericEncoder.encoder());
	}

	public static PropertyBuilder dataObjectProperty() {
		return property(DataObjectPropertyReader.reader());
	}

	private PropertyBuilder(PropertyReader reader, Encoder encoder) {
		this.encoder = encoder;
		this.reader = reader;
	}

	public PropertyBuilder encodedWith(Encoder encoder) {
		this.encoder = encoder;
		return this;
	}

	@Override
	public void encode(Object root, String propertyName, JsonGenerator out) throws IOException {
		Object value = root == null ? null : read(root, propertyName);
		encoder.encode(propertyName, value, out);
	}

	@Override
	public Object read(Object root, String propertyName) {
		return reader.value(root, propertyName);
	}

	@Override
	public int visit(Object root, String propertyName, EncoderVisitor visitor) {
		Object value = root == null ? null : read(root, propertyName);
		return encoder.visitEntities(value, visitor);
	}
}
