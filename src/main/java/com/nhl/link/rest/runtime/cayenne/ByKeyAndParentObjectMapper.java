package com.nhl.link.rest.runtime.cayenne;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.UpdateResponse;

/**
 * An {@link ObjectMapper} that locates objects by the combination of FK to
 * parent and some other column. I.e. those objects in 1..N relationships that
 * have a unique property within parent.
 * 
 * @since 1.4
 */
public class ByKeyAndParentObjectMapper extends CayenneObjectMapper {

	private String property;

	public static ByKeyAndParentObjectMapper byKeyAndParent(String key) {
		return new ByKeyAndParentObjectMapper(key);
	}
	
	public static ByKeyAndParentObjectMapper byKeyAndParent(Property<?> key) {
		return new ByKeyAndParentObjectMapper(key.getName());
	}

	private ByKeyAndParentObjectMapper(String property) {
		this.property = property;
	}

	@Override
	protected <T> ResponseObjectMapper<T> create(UpdateResponse<T> response, ObjectContext context) {
		return new ByKeyAndParentResponseObjectMapper<>(response, context, property);
	}
}
