package io.agrest.meta;

import io.agrest.compiler.AgEntityCompiler;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An {@link AgSchema} that lazily compiles its entities as they are accessed. Lazy compilation helps resolve circular
 * relationships, as well as results in faster startup times.
 *
 * @since 5.0
 */
public class LazySchema implements AgSchema {

    private final Collection<AgEntityCompiler> compilers;
    private final ConcurrentMap<Class<?>, AgEntity<?>> entities;

    public LazySchema(Collection<AgEntityCompiler> compilers) {
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
