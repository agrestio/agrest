package com.nhl.link.rest.runtime.cayenne.processor;

import java.util.function.Function;

import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;

public class CayenneIdMapper implements Function<Object, ObjectId> {

	private static final CayenneIdMapper INSTANCE = new CayenneIdMapper();

	public static CayenneIdMapper instance() {
		return INSTANCE;
	}

	@Override
	public ObjectId apply(Object t) {
		return ((Persistent) t).getObjectId();
	}
}
