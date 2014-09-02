package com.nhl.link.rest.runtime.cayenne;

import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.UpdateResponse;

/**
 * @since 1.7
 */
class UpdateObjectLocator implements ObjectLocator {

	private static final ObjectLocator instance = new UpdateObjectLocator();

	static ObjectLocator instance() {
		return instance;
	}

	private UpdateObjectLocator() {
	}

	@Override
	public <T> Map<EntityUpdate, T> locate(UpdateResponse<T> response, ResponseObjectMapper<T> mapper) {

		Map<EntityUpdate, T> existing = mapper.find();

		// find and report any missing objects
		for (EntityUpdate u : response.getUpdates()) {

			if (existing.get(u) == null) {
				ObjEntity entity = response.getEntity().getCayenneEntity();
				throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + u.getId() + "' and entity '"
						+ entity.getName() + "'");
			}
		}

		return existing;
	}

}
