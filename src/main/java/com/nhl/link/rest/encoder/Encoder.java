package com.nhl.link.rest.encoder;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * An object that encodes JSON field/value pairs.
 */
public interface Encoder {

	/**
	 * Encodes provided object into {@link JsonGenerator}. Encoder should encode
	 * "propertyName" (if not null) and a matching object.
	 * 
	 * @param propertyName
	 *            Specifies the "incoming" property that points to the current
	 *            object from its parent object. This argument can be null, in
	 *            which case we are dealing with a root object, and property
	 *            name should not be encoded.
	 * @param object
	 *            object to encode
	 * @param out
	 *            output object where encoded JSON should be written.
	 * @since 6.9 this method returns boolean instead of void.
	 */
	boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException;

	/**
	 * Allows to check whether a given object will be encoded or skipped by the
	 * current encoder. This is the same as
	 * {@link #encode(String, Object, JsonGenerator)}, only without actually
	 * encoding the object. performing
	 * 
	 * @since 6.9
	 */
	boolean willEncode(String propertyName, Object object);
}
