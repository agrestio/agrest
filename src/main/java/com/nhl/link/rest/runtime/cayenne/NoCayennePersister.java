package com.nhl.link.rest.runtime.cayenne;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.EntityResolver;

/**
 * A placeholder for {@link ICayennePersister} for LinkRest containers that work
 * with POJOs.
 */
public final class NoCayennePersister implements ICayennePersister {
	
	private static final ICayennePersister INSTANCE = new NoCayennePersister();
	
	public static ICayennePersister instance() {
		return INSTANCE;
	}

	private EntityResolver emptyResolver;
	
	public NoCayennePersister() {
		this.emptyResolver = new EntityResolver();
	}

	@Override
	public EntityResolver entityResolver() {
		return emptyResolver;
	}

	@Override
	public ObjectContext newContext() {
		throw new UnsupportedOperationException("This service does not support Cayenne interaction");
	}

	@Override
	public ObjectContext sharedContext() {
		throw new UnsupportedOperationException("This service does not support Cayenne interaction");
	}
}
