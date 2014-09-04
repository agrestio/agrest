package com.nhl.link.rest.runtime.cayenne;

import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.UpdateResponse;

/**
 * A superclass of {@link ObjectMapperFactory}'s based on Cayenne backend.
 * 
 * @since 1.4
 */
public abstract class CayenneObjectMapperFactory implements ObjectMapperFactory {

	@Override
	public <T> ObjectMapper<T> forResponse(UpdateResponse<T> response) {
		if (!(response instanceof CayenneUpdateResponse)) {
			throw new IllegalArgumentException("Expected CayenneUpdateResponse");
		}

		return mapper(response);
	}

	protected abstract <T> ObjectMapper<T> mapper(UpdateResponse<T> response);
}
