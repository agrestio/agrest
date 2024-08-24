package io.agrest.cayenne.persister;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.runtime.CayenneRuntime;

public class CayennePersister implements ICayennePersister {

	private CayenneRuntime runtime;
	private ObjectContext sharedContext;

	public CayennePersister(CayenneRuntime runtime) {

		if (runtime == null) {
			throw new NullPointerException("Null runtime");
		}

		this.runtime = runtime;
		this.sharedContext = runtime.newContext();
	}

	@Override
	public ObjectContext sharedContext() {
		return sharedContext;
	}

	@Override
	public ObjectContext newContext() {
		return runtime.newContext();
	}

	@Override
	public EntityResolver entityResolver() {
		return runtime.getChannel().getEntityResolver();
	}
}
