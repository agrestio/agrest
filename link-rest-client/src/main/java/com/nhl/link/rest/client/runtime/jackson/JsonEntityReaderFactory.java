package com.nhl.link.rest.client.runtime.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.client.LinkRestClientException;
import com.nhl.link.rest.client.runtime.jackson.compiler.JsonEntityReaderCompiler;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 2.0
 */
public class JsonEntityReaderFactory implements IJsonEntityReaderFactory {

	private Collection<JsonEntityReaderCompiler> compilers;
	private Map<Class<?>, IJsonEntityReader<?>> readerMap;

	public JsonEntityReaderFactory(Collection<JsonEntityReaderCompiler> compilers) {
		this.compilers = compilers;

		Map<Class<?>, IJsonEntityReader<?>> readerMap = new ConcurrentHashMap<>();
		readerMap.put(JsonNode.class, new JsonEntityReader());
		this.readerMap = readerMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IJsonEntityReader<T> getReaderForType(Class<T> targetType) {
		if (targetType == null) {
			throw new NullPointerException("Missing target type");
		}

		IJsonEntityReader<?> reader = readerMap.get(targetType);
		if (reader == null) {
			if ((reader = compileReader(targetType)) != null) {
				IJsonEntityReader<?> existing = readerMap.putIfAbsent(targetType, reader);
				if (existing != null) {
					reader = existing;
				}
			} else {
				throw new LinkRestClientException("No readers found for type: " + targetType.getName());
			}
		}
		return (IJsonEntityReader<T>) reader;
	}

	private <T> IJsonEntityReader<T> compileReader(Class<T> targetType) {
		IJsonEntityReader<T> reader = null;
		for (JsonEntityReaderCompiler compiler : compilers) {
			reader = compiler.compile(targetType);
			if (reader != null) {
				break;
			}
		}
		return reader;
	}
}
