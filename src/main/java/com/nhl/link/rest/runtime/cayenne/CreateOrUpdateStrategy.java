package com.nhl.link.rest.runtime.cayenne;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.runtime.cayenne.CayenneUpdateBuilder.ObjectRelator;

/**
 * @since 1.7
 */
class CreateOrUpdateStrategy<T> extends UpdateStrategy<T> {

	private boolean idempotent;

	CreateOrUpdateStrategy(CayenneUpdateResponse<T> response, ObjectRelator<T> relator, ResourceReader reader,
			ObjectMapper<T> mapper, boolean idempotent) {
		super(response, relator, reader, mapper);
		this.idempotent = idempotent;
	}

	@Override
	protected void afterUpdatesMerge(Map<Object, Collection<EntityUpdate>> keyMap) {

		if (keyMap.isEmpty()) {
			return;
		}

		for (Entry<Object, Collection<EntityUpdate>> e : keyMap.entrySet()) {

			// null key - each update is individual object to create;
			// explicit key - each update applies to the same object;

			if (e.getKey() == null) {

				if (idempotent) {
					throw new LinkRestException(Status.BAD_REQUEST, "Request is not idempotent.");
				}

				for (EntityUpdate u : e.getValue()) {
					create(Collections.singletonList(u));
				}
			} else {
				create(e.getValue());
			}
		}
	}
}
