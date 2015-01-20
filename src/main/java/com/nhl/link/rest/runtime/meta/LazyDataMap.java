package com.nhl.link.rest.runtime.meta;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrDataMap;

/**
 * A {@link LrDataMap} that lazily creates its entities, improving startup
 * time and decreasing memory footprint. Memory savings come from the fact that
 * the entire Cayenne mapping does not need to be converted to
 * {@link LrDataMap} immediately.
 * 
 * @since 1.12
 */
public class LazyDataMap implements LrDataMap {

	private EntityFactory entityFactory;
	private ConcurrentMap<Class<?>, LrEntity<?>> entities;

	public LazyDataMap(EntityFactory entityFactory) {
		this.entities = new ConcurrentHashMap<>();
		this.entityFactory = entityFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LrEntity<T> getEntity(Class<T> type) {
		LrEntity<?> e = entities.get(type);

		if (e == null) {

			LrEntity<?> newEntity = entityFactory.createEntity(type);
			LrEntity<?> existingEntity = entities.putIfAbsent(type, newEntity);
			e = existingEntity != null ? existingEntity : newEntity;
		}

		return (LrEntity<T>) e;
	}

}
