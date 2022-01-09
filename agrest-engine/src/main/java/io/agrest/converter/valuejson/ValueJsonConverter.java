package io.agrest.converter.valuejson;

/**
 * A converter that converts objects to Strings, usually for the purpose of JSON encoding.
 *
 * @since 5.0
 */
public interface ValueJsonConverter {

    String asString(Object object);
}
