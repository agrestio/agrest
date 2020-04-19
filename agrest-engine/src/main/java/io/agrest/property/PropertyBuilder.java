package io.agrest.property;

import io.agrest.EntityProperty;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncoderVisitor;
import io.agrest.encoder.GenericEncoder;

/**
 * A {@link EntityProperty} implementation that provides fluent builder methods for the manual property assembly.
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

	private PropertyBuilder(PropertyReader reader, Encoder encoder) {
		this.encoder = encoder;
		this.reader = reader;
	}

	public PropertyBuilder encodedWith(Encoder encoder) {
		this.encoder = encoder;
		return this;
	}

	@Override
	public Encoder getEncoder() {
		return encoder;
	}

	@Override
	public PropertyReader getReader() {
		return reader;
	}

	@Override
	public int visit(Object root, String propertyName, EncoderVisitor visitor) {
		Object value = root == null ? null : getReader().value(root, propertyName);
		return encoder.visitEntities(value, visitor);
	}
}
