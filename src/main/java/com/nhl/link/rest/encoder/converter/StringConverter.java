package com.nhl.link.rest.encoder.converter;

/**
 * A converter that converts objects to Strings. JSON field names can only be
 * Strings. So this converter is useful when generating JSON with field names
 * mapped to arbitrary objects.
 */
public interface StringConverter {

	String asString(Object object);
}
