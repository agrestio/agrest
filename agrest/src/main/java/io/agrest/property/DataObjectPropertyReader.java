package io.agrest.property;

import org.apache.cayenne.DataObject;
import org.apache.cayenne.Fault;

import java.util.List;

public class DataObjectPropertyReader implements PropertyReader {

	private static final PropertyReader instance = new DataObjectPropertyReader();

	public static PropertyReader reader() {
		return instance;
	}

	@Override
	public Object value(Object root, String name) {
		// unwraps a single object from collection
		// TODO provide a fix in right place to avoid that situation
		if (root instanceof List) {
			root = ((List)root).get(0);
		}
		// reads plain object property only, without child relations
		Object result = ((DataObject) root).readPropertyDirectly(name);
		if (result instanceof Fault) {
			return null;
		}
		return result;
	}

}
