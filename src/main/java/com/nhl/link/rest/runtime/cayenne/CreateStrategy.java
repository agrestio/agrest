package com.nhl.link.rest.runtime.cayenne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.runtime.cayenne.CayenneUpdateBuilder.ObjectRelator;

/**
 * @since 1.7
 */
class CreateStrategy<T> extends BaseSyncStrategy<T> {

	CreateStrategy(CayenneUpdateResponse<T> response, ObjectRelator<T> relator) {
		super(response, relator);
	}

	@Override
	protected List<T> doSync() {

		Collection<EntityUpdate> updates = response.getUpdates();

		// sizing the list with one object per update assumption, which I guess
		// is correct in case of pure 'create' op

		List<T> result = new ArrayList<>(updates.size());

		for (EntityUpdate u : updates) {
			T o = create(Collections.singleton(u));
			result.add(o);
		}

		return result;
	}
}
