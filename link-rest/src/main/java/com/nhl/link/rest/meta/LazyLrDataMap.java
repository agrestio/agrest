package com.nhl.link.rest.meta;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.nhl.link.rest.meta.compiler.LrEntityCompiler;

/**
 * An {@link LrDataMap} that lazily loads its entities.
 * 
 * @since 1.24
 */
public class LazyLrDataMap implements LrDataMap {

	private Collection<LrEntityCompiler> compilers;
	private ConcurrentMap<Class<?>, LrEntity<?>> entities;

	public LazyLrDataMap(Collection<LrEntityCompiler> compilers, List<LrEntity<?>> extraEntities) {

		this.compilers = compilers;
		this.entities = new ConcurrentHashMap<>();

		for (LrEntity<?> e : extraEntities) {
			entities.put(e.getType(), e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LrEntity<T> getEntity(Class<T> type) {
		LrEntity<?> e = entities.get(type);

		// lazily create entities, improving startup time
		// and decreasing memory footprint.
		if (e == null) {

			LrEntity<?> newEntity = compile(type);
			LrEntity<?> existingEntity = entities.putIfAbsent(type, newEntity);
			e = existingEntity != null ? existingEntity : newEntity;
		}

		return (LrEntity<T>) e;
	}

	protected <T> LrEntity<T> compile(Class<T> type) {

		for (LrEntityCompiler compiler : compilers) {
			LrEntity<T> e = compiler.compile(type);
			if (e != null) {
				return e;
			}
		}

		throw new IllegalArgumentException("Unable to compile LrEntity: " + type);
	}
}
