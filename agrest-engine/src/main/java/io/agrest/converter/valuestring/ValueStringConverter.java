package io.agrest.converter.valuestring;

/**
 * A converter that converts objects to Strings, usually for the purpose of JSON encoding.
 *
 * @since 5.0
 */
public interface ValueStringConverter {

    String asString(Object object);
}
