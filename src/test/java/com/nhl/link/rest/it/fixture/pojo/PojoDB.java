package com.nhl.link.rest.it.fixture.pojo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PojoDB {

	private ConcurrentMap<Class<?>, Map<Object, Object>> map;

	public PojoDB() {
		this.map = new ConcurrentHashMap<>();
	}
	
	public void clear() {
		map.clear();
	}

	@SuppressWarnings("unchecked")
	public <T> Map<Object, T> bucketForType(Class<T> type) {
		Map<Object, Object> bucket = map.get(type);

		if (bucket == null) {

			bucket = new ConcurrentHashMap<>();
			Map<Object, Object> existing = map.putIfAbsent(type, bucket);
			if (existing != null) {
				bucket = existing;
			}
		}

		return (Map<Object, T>) bucket;
	}

}
