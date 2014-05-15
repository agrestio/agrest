package com.nhl.link.rest;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Encapsulates methods for extracting object properties, and encoding them into
 * a JSON stream. Property doesn't necessarily needs to be a JavaBean type
 * property. It can be some value calculated dynamically based on the request
 * context, such as current date, or some other parameters.
 */
public interface ClientProperty {

	/**
	 * Reads a property of a given object and encodes it to the provided JSON
	 * output.
	 */
	void encode(Object root, String propertyName, JsonGenerator out) throws IOException;
}
