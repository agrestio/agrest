package com.nhl.link.rest.runtime.cayenne;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.UpdateResponse;

/**
 * @since 1.7
 */
class IdempotentCreateOrUpdateObjectLocator implements ObjectLocator {

	private static final ObjectLocator instance = new IdempotentCreateOrUpdateObjectLocator();

	static ObjectLocator instance() {
		return instance;
	}

	private IdempotentCreateOrUpdateObjectLocator() {
	}

	@Override
	public <T> Map<EntityUpdate, T> locate(UpdateResponse<T> response, ResponseObjectMapper<T> mapper) {

		Map<EntityUpdate, T> existing = mapper.find();
		Map<EntityUpdate, T> result = null;

		// create missing objects, check for idempotency
		for (EntityUpdate u : response.getUpdates()) {

			if (!mapper.isIdempotent(u)) {
				throw new LinkRestException(Status.BAD_REQUEST, "Request is not idempotent.");
			}

			if (existing.get(u) == null) {
				if (result == null) {
					// since we'll be modifying the result, and "existing" may
					// potentially be cached, clone it here...
					result = new HashMap<>(existing);
				}

				result.put(u, mapper.create(u));
			}
		}

		// if there were no created objects, we can just return existing without
		// cloning...
		return result != null ? result : existing;
	}

}
