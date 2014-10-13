package com.nhl.link.rest.runtime.cayenne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.runtime.cayenne.CayenneUpdateBuilder.ObjectRelator;

/**
 * @since 1.7
 */
class UpdateStrategy<T> extends BaseSyncStrategy<T> {

	protected ObjectMapper<T> mapper;
	protected ResourceReader reader;

	UpdateStrategy(CayenneUpdateResponse<T> response, ObjectRelator<T> relator, ResourceReader reader,
			ObjectMapper<T> mapper) {
		super(response, relator);
		this.mapper = mapper;
		this.reader = reader;
	}

	@Override
	protected void doSync() {

		Map<Object, Collection<EntityUpdate>> keyMap = mutableKeyMap();
		List<T> objectsToUpdate = reader.itemsForKeys(response, keyMap.keySet(), mapper);

		for (T o : objectsToUpdate) {
			Object key = mapper.keyForObject(o);

			Collection<EntityUpdate> updates = keyMap.remove(key);

			// a null can only mean some algorithm malfunction
			if (updates == null) {
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Invalid key item: " + key);
			}

			update(updates, o);
		}

		// check leftovers - those correspond to objects missing in the DB or
		// objects with no keys
		afterUpdatesMerge(keyMap);
	}

	protected void afterUpdatesMerge(Map<Object, Collection<EntityUpdate>> keyMap) {
		if (!keyMap.isEmpty()) {
			Object firstKey = keyMap.keySet().iterator().next();

			if (firstKey == null) {
				throw new LinkRestException(Status.BAD_REQUEST, "Can't update. No id for object");
			}

			ObjEntity entity = response.getEntity().getCayenneEntity();
			throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + firstKey + "' and entity '"
					+ entity.getName() + "'");
		}
	}

	protected Map<Object, Collection<EntityUpdate>> mutableKeyMap() {

		Collection<EntityUpdate> updates = response.getUpdates();

		// sizing the map with one-update per key assumption
		Map<Object, Collection<EntityUpdate>> map = new HashMap<>((int) (updates.size() / 0.75));

		for (EntityUpdate u : response.getUpdates()) {

			Object key = mapper.keyForUpdate(u);
			Collection<EntityUpdate> updatesForKey = map.get(key);
			if (updatesForKey == null) {
				updatesForKey = new ArrayList<>(2);
				map.put(key, updatesForKey);
			}

			updatesForKey.add(u);
		}

		return map;
	}
}
