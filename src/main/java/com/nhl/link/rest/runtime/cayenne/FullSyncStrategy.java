package com.nhl.link.rest.runtime.cayenne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.cayenne.DataObject;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.runtime.cayenne.CayenneUpdateBuilder.ObjectRelator;

/**
 * @since 1.7
 */
class FullSyncStrategy<T> extends CreateOrUpdateStrategy<T> {

	public FullSyncStrategy(CayenneUpdateResponse<T> response, ObjectRelator<T> relator, ResourceReader reader,
			ObjectMapper<T> mapper, boolean idempotent) {
		super(response, relator, reader, mapper, idempotent);
	}

	@Override
	protected List<T> doSync() {

		Map<Object, Collection<EntityUpdate>> keyMap = mutableKeyMap();
		List<T> allObjects = reader.allItems(response);

		// can only guess the capacity of 'remainingObjects'..
		List<T> remainingObjects = new ArrayList<>(allObjects.size());
		List<DataObject> deletedObjects = new ArrayList<>();

		for (T o : allObjects) {
			Object key = mapper.keyForObject(o);

			Collection<EntityUpdate> updates = keyMap.remove(key);

			if (updates == null) {
				deletedObjects.add((DataObject) o);
			} else {
				update(updates, o);
				remainingObjects.add(o);
			}
		}

		if (!deletedObjects.isEmpty()) {
			response.getUpdateContext().deleteObjects(deletedObjects);
		}

		// check leftovers - those correspond to objects missing in the DB or
		// objects with no keys
		return afterUpdatesMerge(keyMap, remainingObjects);
	}

}
