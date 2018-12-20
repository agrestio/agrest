package io.agrest.runtime.cayenne;

import io.agrest.runtime.IAgPersister;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.map.EntityResolver;

public class CayennePersister implements IAgPersister<ObjectContext, EntityResolver> {

	private ServerRuntime runtime;
	private ObjectContext sharedContext;

	public CayennePersister(ServerRuntime runtime) {

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
