package io.agrest.cayenne.compiler;

import io.agrest.property.PropertyReader;
import org.apache.cayenne.DataObject;

public class DataObjectPropertyReader implements PropertyReader {

	private final String propertyName;

	public static PropertyReader reader(String propertyName) {
		return new DataObjectPropertyReader(propertyName);
	}

	protected DataObjectPropertyReader(String propertyName) {
		this.propertyName = propertyName;
	}

	@Override
	public Object value(Object root) {
		return ((DataObject) root).readProperty(propertyName);
	}
}
