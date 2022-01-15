package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

/**
 * An object that encodes JSON field/value pairs.
 */
public interface Encoder {

	int VISIT_CONTINUE = 0b00;
	int VISIT_SKIP_CHILDREN = 0b01;
	int VISIT_SKIP_ALL = 0b10;

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
	 */
	boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException;

	/**
	 * A graph traversal method that recursively visits all entity nodes starting with provided object.
	 * <p>
	 * Visitor can control ongoing graph traversal by returning a code from each visit callback. Code is a bitmask
	 * combining one of {@link #VISIT_CONTINUE}, {@link #VISIT_SKIP_ALL}, {@link #VISIT_SKIP_CHILDREN}.
	 * 
	 * @since 2.0
	 */
	default int visitEntities(Object object, EncoderVisitor visitor) {
		if (object != null) {
			int bitmask = visitor.visit(object);
			return (bitmask & VISIT_SKIP_ALL) != 0 ? VISIT_SKIP_ALL : VISIT_CONTINUE;
		}

		return VISIT_CONTINUE;
	}
}
