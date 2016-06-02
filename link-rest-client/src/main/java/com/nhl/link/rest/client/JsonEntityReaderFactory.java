package com.nhl.link.rest.client;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 2.0
 */
public class JsonEntityReaderFactory implements IJsonEntityReaderFactory {

	private Map<Class<?>, IJsonEntityReader<?>> readerMap;

	JsonEntityReaderFactory() {
		readerMap = new HashMap<>();
		readerMap.put(JsonNode.class, new JsonEntityReader());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IJsonEntityReader<T> getReaderForType(Class<T> targetType) {

		if (targetType == null) {
			throw new NullPointerException("Target type");
		}
		return (IJsonEntityReader<T>) readerMap.get(targetType);
	}
}
