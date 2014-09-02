package com.nhl.link.rest.runtime.cayenne;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.UpdateResponse;

/**
 * A default singleton implementation of the {@link ObjectMapper} that looks up
 * objects based on their IDs.
 * 
 * @since 1.4
 */
public class ByIdObjectMapper extends CayenneObjectMapper {

	private static final ObjectMapper instance = new ByIdObjectMapper();

	public static ObjectMapper mapper() {
		return instance;
	}

	@Override
	protected <T> ResponseObjectMapper<T> create(UpdateResponse<T> response, ObjectContext context) {

		ObjEntity entity = response.getEntity().getCayenneEntity();
		// sanity checking...
		if (entity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unknown entity class: " + response.getType());
		}

		String keyPath = "db:" + entity.getPrimaryKeyNames().iterator().next();
		// TODO: multi-column ids

		return new ByIdResponseObjectMapper<>(response, context, keyPath);
	}
}
