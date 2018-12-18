package io.agrest.runtime.cayenne;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.EntityResolver;

public interface ICayennePersister {

	ObjectContext sharedContext();

	ObjectContext newContext();

	EntityResolver entityResolver();

}
