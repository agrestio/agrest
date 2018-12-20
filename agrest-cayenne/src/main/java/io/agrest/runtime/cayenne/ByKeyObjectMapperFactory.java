package io.agrest.runtime.cayenne;

import io.agrest.ObjectMapper;
import io.agrest.ObjectMapperFactory;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.exp.Property;

/**
 * An {@link ObjectMapperFactory} that locates objects by the combination of FK
 * to parent and some other column. I.e. those objects in 1..N relationships
 * that have a unique property within parent.
 * 
 * @since 1.4
 */
public class ByKeyObjectMapperFactory implements ObjectMapperFactory {

	private String property;

	public static ByKeyObjectMapperFactory byKey(String key) {
		return new ByKeyObjectMapperFactory(key);
	}

	public static ByKeyObjectMapperFactory byKey(Property<?> key) {
		return new ByKeyObjectMapperFactory(key.getName());
	}

	private ByKeyObjectMapperFactory(String property) {
		this.property = property;
	}

	@Override
	public <T, E> ObjectMapper<T, E> createMapper(UpdateContext<T, E> context) {
		return (ObjectMapper<T, E>) new ByKeyObjectMapper<>(property);
	}
}
