package com.nhl.link.rest.client;

/**
 * @since 2.0
 */
public interface IJsonEntityReaderFactory {

    <T> IJsonEntityReader<T> getReaderForType(Class<T> targetType);
}
