package io.agrest.property;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.EntityProperty;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncoderVisitor;
import io.agrest.encoder.GenericEncoder;

/**
 * A {@link EntityProperty} implementation that provides fluent builder methods
 * for the manual property assembly.
 */
public class PropertyBuilder implements EntityProperty {

	private static final EntityProperty DO_NOTHING_PROPERTY;

	static {
		DO_NOTHING_PROPERTY = new EntityProperty() {

			@Override
			public void encode(Object root, String propertyName, JsonGenerator out) throws IOException {
				// do nothing...
			}

			@Override
			public Object read(Object root, String propertyName) {
				throw new UnsupportedOperationException("Can't read property: " + propertyName);
			}

			@Override
			public int visit(Object root, String propertyName, EncoderVisitor visitor) {
				return Encoder.VISIT_CONTINUE;
			}
		};
	}

	private Encoder encoder;
	private PropertyReader reader;
	private BiFunction<Object, String, List<?>> childResolver;

	public static EntityProperty doNothingProperty() {
		return DO_NOTHING_PROPERTY;
	}

	public static PropertyBuilder property() {
		return new PropertyBuilder(BeanPropertyReader.reader(), GenericEncoder.encoder(), (i, n) -> Collections.emptyList());
	}

	public static PropertyBuilder property(PropertyReader reader) {
		return new PropertyBuilder(reader, GenericEncoder.encoder(), (i, n) -> Collections.emptyList());
	}

	public static PropertyBuilder dataObjectProperty() {
		return property(DataObjectPropertyReader.reader());
	}

	private PropertyBuilder(PropertyReader reader, Encoder encoder, BiFunction<Object, String, List<?>> childResolver) {
		this.encoder = encoder;
		this.reader = reader;
		this.childResolver = childResolver;
	}

	public PropertyBuilder encodedWith(Encoder encoder) {
		this.encoder = encoder;
		return this;
	}

	public PropertyBuilder resolveChildren(BiFunction<Object, String, List<?>> childResolver) {
		this.childResolver = childResolver;
		return this;
	}

	@Override
	public void encode(Object root, String propertyName, JsonGenerator out) throws IOException {
		Object value = root == null ? null : read(root, propertyName);
		encoder.encode(propertyName, value, out);
	}

	@Override
	public Object read(Object root, String propertyName) {
		Object value = reader.value(root, propertyName);

		if (value == null) {
			// try to read children
			value = childResolver.apply(root, propertyName);
		}
		return value;
	}

	@Override
	public int visit(Object root, String propertyName, EncoderVisitor visitor) {
		Object value = root == null ? null : read(root, propertyName);
		return encoder.visitEntities(value, visitor);
	}
}
