package io.agrest.meta;

import io.agrest.compiler.AgEntityCompiler;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An {@link AgDataMap} that lazily loads its entities.
 * 
 * @since 1.24
 */
public class LazyAgDataMap implements AgDataMap {

	private Collection<AgEntityCompiler> compilers;
	private ConcurrentMap<Class<?>, AgEntity<?>> entities;

	public LazyAgDataMap(Collection<AgEntityCompiler> compilers) {

		this.compilers = compilers;
		this.entities = new ConcurrentHashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> AgEntity<T> getEntity(Class<T> type) {
		// lazily create entities, improving startup time and decreasing memory footprint.
		return (AgEntity<T>) entities.computeIfAbsent(type, t -> compile(type));
	}

	protected <T> AgEntity<T> compile(Class<T> type) {

		for (AgEntityCompiler compiler : compilers) {
			AgEntity<T> e = compiler.compile(type, this);
			if (e != null) {
				return e;
			}
		}

		throw new IllegalArgumentException("Unable to compile AgEntity: " + type);
	}
}
