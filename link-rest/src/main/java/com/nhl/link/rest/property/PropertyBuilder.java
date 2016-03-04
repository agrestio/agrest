package com.nhl.link.rest.property;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.GenericEncoder;

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
		};
	}

	private Encoder encoder;
	private PropertyReader reader;

	public static EntityProperty doNothingProperty() {
		return DO_NOTHING_PROPERTY;
	}

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
		Object value = root == null ? null : reader.value(root, propertyName);
		encoder.encode(propertyName, value, out);
	}
}
