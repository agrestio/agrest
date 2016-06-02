package com.nhl.link.rest.client;

public interface IJsonEntityReaderFactory {

    <T> IJsonEntityReader<T> getReaderForType(Class<T> targetType);
}
