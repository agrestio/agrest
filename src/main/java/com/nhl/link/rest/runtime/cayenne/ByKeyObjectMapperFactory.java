package com.nhl.link.rest.runtime.cayenne;

import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.UpdateResponse;

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
	public <T> ObjectMapper<T> forResponse(UpdateResponse<T> response) {
		return new ByKeyObjectMapper<>(property);
	}
}
