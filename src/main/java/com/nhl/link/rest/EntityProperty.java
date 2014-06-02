package com.nhl.link.rest;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Encapsulates how certain data is extracted and encoded from entity objects.
 * Conceptually {@link EntityProperty} is not limited to object "bean"
 * properties. It can be some value calculated dynamically based on the request
 * context, such as current date, or some other parameters, etc.
 */
public interface EntityProperty {

	/**
	 * Reads a property of a given object and encodes it to the provided JSON
	 * output.
	 */
	void encode(Object root, String propertyName, JsonGenerator out) throws IOException;
}
