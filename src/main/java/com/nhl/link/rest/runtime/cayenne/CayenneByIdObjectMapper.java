package com.nhl.link.rest.runtime.cayenne;

import org.apache.cayenne.ObjectContext;

import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.UpdateResponse;

/**
 * A default implementation of the {@link ObjectMapper} that maps objects based
 * on their IDs.
 * 
 * @since 1.4
 */
public class CayenneByIdObjectMapper extends CayenneObjectMapper {

	private static final ObjectMapper instance = new CayenneByIdObjectMapper();

	public static ObjectMapper mapper() {
		return instance;
	}

	@Override
	protected <T> ResponseObjectMapper<T> create(UpdateResponse<T> response, ObjectContext context) {
		return new CayenneByIdResponseObjectMapper<>(response, context);
	}
}
