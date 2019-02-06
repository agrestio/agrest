package io.agrest.property;

import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;

public class PersistentObjectIdPropertyReader implements PropertyReader {

	private static final PropertyReader instance = new PersistentObjectIdPropertyReader();

	public static PropertyReader reader() {
		return instance;
	}

	@Override
	public Object value(Object root, String name) {

		ObjectId id = ((Persistent) root).getObjectId();
		if (id.isTemporary()) {
			// for now supporting only permanent IDs
			throw new IllegalArgumentException("Can't read from temporary ObjectId: " + id);
		}
		return id.getIdSnapshot().get(name);
	}

}