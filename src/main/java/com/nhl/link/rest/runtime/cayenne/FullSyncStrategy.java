package com.nhl.link.rest.runtime.cayenne;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.runtime.cayenne.CayenneUpdateBuilder.ObjectRelator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since 1.7
 */
class FullSyncStrategy<T> extends CreateOrUpdateStrategy<T> {

	private boolean onDeleteUnrelate;

	public FullSyncStrategy(CayenneUpdateResponse<T> response, ObjectRelator<T> relator, ResourceReader reader,
			ObjectMapper<T> mapper, boolean idempotent, boolean onDeleteUnrelate) {
		super(response, relator, reader, mapper, idempotent);

		this.onDeleteUnrelate = onDeleteUnrelate;
	}

	@Override
	protected void doSync() {

		Map<Object, Collection<EntityUpdate>> keyMap = mutableKeyMap();
		List<T> allObjects = reader.allItems(response);

		List<T> deletedObjects = new ArrayList<>();

		for (T o : allObjects) {
			Object key = mapper.keyForObject(o);

			Collection<EntityUpdate> updates = keyMap.remove(key);

			if (updates == null) {
				deletedObjects.add(o);
			} else {
				update(updates, o);
			}
		}

		if (!deletedObjects.isEmpty()) {
			if (onDeleteUnrelate) {
				unrelate(deletedObjects);
			} else {
				delete(deletedObjects);
			}
		}

		// check leftovers - those correspond to objects missing in the DB or
		// objects with no keys
		afterUpdatesMerge(keyMap);
	}

}
