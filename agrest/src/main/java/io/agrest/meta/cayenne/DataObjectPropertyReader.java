package io.agrest.meta.cayenne;

import io.agrest.property.PropertyReader;
import org.apache.cayenne.DataObject;

public class DataObjectPropertyReader implements PropertyReader {

	private static final PropertyReader instance = new DataObjectPropertyReader();

	public static PropertyReader reader() {
		return instance;
	}

	@Override
	public Object value(Object root, String name) {
		return ((DataObject) root).readProperty(name);
	}
}
