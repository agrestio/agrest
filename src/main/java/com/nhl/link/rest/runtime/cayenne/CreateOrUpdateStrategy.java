package com.nhl.link.rest.runtime.cayenne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
	protected List<T> afterUpdatesMerge(Map<Object, Collection<EntityUpdate>> keyMap, List<T> result) {

		if (keyMap.isEmpty()) {
			return result;
		}

		// must clone result, tne original came from Cayenne and can be cached,
		// or can be an immutable list, etc.

		// initial capacity is just a guess.. each 'keyMap' entry may resolve to
		// multiple objects
		List<T> amendedResult = new ArrayList<>(result.size() + keyMap.size());
		amendedResult.addAll(result);

		for (Entry<Object, Collection<EntityUpdate>> e : keyMap.entrySet()) {

			// null key - each update is individual object to create;
			// explicit key - each update applies to the same object;

			if (e.getKey() == null) {

				if (idempotent) {
					throw new LinkRestException(Status.BAD_REQUEST, "Request is not idempotent.");
				}

				for (EntityUpdate u : e.getValue()) {
					amendedResult.add(create(Collections.singletonList(u)));
				}
			} else {
				T o = create(e.getValue());
				amendedResult.add(o);
			}
		}

		return amendedResult;
	}
}
