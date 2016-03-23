package com.nhl.link.rest.property;

import org.apache.cayenne.reflect.PropertyUtils;

public class BeanPropertyReader implements PropertyReader {

	private static final PropertyReader instance = new BeanPropertyReader();

	public static PropertyReader reader() {
		return instance;
	}

	public static PropertyReader reader(final String fixedPropertyName) {
		return new PropertyReader() {

			@Override
			public Object value(Object root, String name) {
				return PropertyUtils.getProperty(root, fixedPropertyName);
			}
		};
	}

	@Override
	public Object value(Object root, String name) {
		return PropertyUtils.getProperty(root, name);
	}
}
