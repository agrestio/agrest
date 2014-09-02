package com.nhl.link.rest.runtime.cayenne;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.UpdateResponse;

/**
 * @since 1.7
 */
class CreateObjectLocator implements ObjectLocator {

	private static final ObjectLocator instance = new CreateObjectLocator();

	static ObjectLocator instance() {
		return instance;
	}

	private CreateObjectLocator() {
	}

	@Override
	public <T> Map<EntityUpdate, T> locate(UpdateResponse<T> response, ResponseObjectMapper<T> mapper) {

		Collection<EntityUpdate> updates = response.getUpdates();

		Map<EntityUpdate, T> map = new HashMap<>((int) (updates.size() / 0.75));

		for (EntityUpdate u : updates) {
			T o = mapper.create(u);
			map.put(u, o);
		}

		return map;
	}
}
