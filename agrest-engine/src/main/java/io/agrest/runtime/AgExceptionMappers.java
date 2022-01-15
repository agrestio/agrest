package io.agrest.runtime;

import io.agrest.AgException;
import io.agrest.reflect.Types;
import io.agrest.spi.AgExceptionMapper;
import org.apache.cayenne.di.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Converts various exceptions to {@link AgException} using a preconfigured collection of per-Exception
 * {@link AgExceptionMapper} "mappers".
 *
 * @since 5.0
 */
public class AgExceptionMappers {

    private final ConcurrentMap<Class<?>, AgExceptionMapper<?>> exceptionMappers;
    private final AgExceptionMapper<?> defaultMapper;

    public AgExceptionMappers(@Inject Map<String, AgExceptionMapper<?>> exceptionMappers) {
        // hide exception details from the client, providing a generic message
        this.defaultMapper = th -> AgException.internalServerError(th, "Exception processing Agrest request");

        // convert to a mutable map with a key of Class<?> ... Must be mutable as we will expand the map dynamically
        // if mapped exceptions subclasses are thrown in the app
        this.exceptionMappers = new ConcurrentHashMap<>();
        exceptionMappers.forEach((k, v) -> this.exceptionMappers.put(Types.typeForName(k), v));
    }

    public AgException toAgException(Throwable th) {
        AgExceptionMapper mapper = locateMapper(th.getClass());
        return mapper.toAgException(th);
    }

    AgExceptionMapper<?> defaultMapper() {
        return defaultMapper;
    }

    AgExceptionMapper<?> locateMapper(Class<? extends Throwable> exceptionType) {
        // cache mappers for exception hierarchies
        return exceptionMappers.computeIfAbsent(exceptionType, t -> getOrCreateMapperInHierarchy(t.getSuperclass()));
    }

    private AgExceptionMapper<?> getOrCreateMapperInHierarchy(Class<?> exceptionType) {

        if (Object.class.equals(exceptionType)) {
            return defaultMapper;
        }

        AgExceptionMapper<?> mapper = exceptionMappers.get(exceptionType);
        if (mapper != null) {
            return mapper;
        }

        return getOrCreateMapperInHierarchy(exceptionType.getSuperclass());
    }
}
