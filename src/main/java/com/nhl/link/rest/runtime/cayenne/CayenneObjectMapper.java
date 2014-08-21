package com.nhl.link.rest.runtime.cayenne;

import org.apache.cayenne.ObjectContext;

import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.UpdateResponse;

/**
 * A superclass of {@link ObjectMapper}'s based on Cayenne backend.
 * 
 * @since 1.4
 */
public abstract class CayenneObjectMapper implements ObjectMapper {

	@Override
	public <T> ResponseObjectMapper<T> forResponse(UpdateResponse<T> response) {
		if (!(response instanceof CayenneUpdateResponse)) {
			throw new IllegalArgumentException("Expected CayenneUpdateResponse");
		}

		return create(response, ((CayenneUpdateResponse<?>) response).getUpdateContext());
	}

	protected abstract <T> ResponseObjectMapper<T> create(UpdateResponse<T> response, ObjectContext context);
}
