package com.nhl.link.rest.client.runtime.jackson;

/**
 * @since 2.0
 */
public interface IJsonEntityReaderFactory {

    <T> IJsonEntityReader<T> getReaderForType(Class<T> targetType);
}
