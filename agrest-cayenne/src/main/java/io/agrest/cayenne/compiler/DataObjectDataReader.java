package io.agrest.cayenne.compiler;

import io.agrest.reader.DataReader;
import org.apache.cayenne.Persistent;

public class DataObjectDataReader implements DataReader {

	private final String propertyName;

	public static DataReader reader(String propertyName) {
		return new DataObjectDataReader(propertyName);
	}

	protected DataObjectDataReader(String propertyName) {
		this.propertyName = propertyName;
	}

	@Override
	public Object read(Object root) {
		return ((Persistent) root).readProperty(propertyName);
	}
}
