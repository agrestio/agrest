package com.nhl.link.rest.runtime.encoder;

import org.apache.cayenne.exp.Property;
import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.converter.StringConverter;

public interface IStringConverterFactory {

	/**
	 * Returns a {@link StringConverter} for a given entity object. Normally the
	 * returned converter is some kind of ID converter.
	 * 
	 * @since 6.5
	 */
	StringConverter getConverter(Class<?> entityType, Property<?> attribute);

	/**
	 * Returns a {@link StringConverter} for a given entity object. Normally the
	 * returned converter is some kind of ID converter.
	 */
	StringConverter getConverter(ObjEntity entity);

	/**
	 * Returns a {@link StringConverter} for a given ObjAttribute.
	 */
	StringConverter getConverter(ObjEntity entity, String attributeName);

}
