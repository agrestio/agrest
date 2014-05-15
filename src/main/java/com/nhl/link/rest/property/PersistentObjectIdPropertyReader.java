package com.nhl.link.rest.property;

import org.apache.cayenne.Persistent;

public class PersistentObjectIdPropertyReader implements PropertyReader {

	private static final PropertyReader instance = new PersistentObjectIdPropertyReader();

	public static PropertyReader reader() {
		return instance;
	}

	@Override
	public Object value(Object root, String name) {
		return ((Persistent) root).getObjectId();
	}

}