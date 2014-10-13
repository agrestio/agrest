package com.nhl.link.rest.runtime.cayenne;

import java.util.Collection;
import java.util.Collections;

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
	protected void doSync() {

		Collection<EntityUpdate> updates = response.getUpdates();

		for (EntityUpdate u : updates) {
			create(Collections.singleton(u));
		}
	}
}
