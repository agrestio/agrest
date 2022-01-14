package io.agrest.runtime;

import io.agrest.AgException;
import io.agrest.reflect.Types;
import io.agrest.spi.AgExceptionMapper;
import org.apache.cayenne.di.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 5.0
 */
public class ExceptionMappers {

    private final ConcurrentMap<Class<?>, AgExceptionMapper<?>> exceptionMappers;
    private final AgExceptionMapper<?> defaultMapper;

    public ExceptionMappers(@Inject Map<String, AgExceptionMapper<?>> exceptionMappers) {
        // hide exception details from the client, providing a generic message
        this.defaultMapper = th -> AgException.internalServerError(th, "Exception processing Agrest request");

        // convert to a mutable map with a key of Class<?> ... We will expand the map dynamically if mapped exceptions
        // subclasses are thrown in the app
        this.exceptionMappers = new ConcurrentHashMap<>();
        exceptionMappers.forEach((k, v) -> this.exceptionMappers.put(Types.typeForName(k), v));
    }

    public AgException toAgException(Throwable th) {
        AgExceptionMapper mapper = locateMapper(th.getClass());
        throw mapper.toAgException(th);
    }

    private AgExceptionMapper<?> locateMapper(Class<? extends Throwable> exceptionType) {
        // cache mappers for exception hierarchies
        return exceptionMappers.computeIfAbsent(exceptionType, t -> getOrCreateMapperInHierarchy(t.getSuperclass()));
    }

    private AgExceptionMapper<?> getOrCreateMapperInHierarchy(Class<?> exceptionType) {

        AgExceptionMapper<?> mapper = exceptionMappers.get(exceptionType);
        if (mapper != null) {
            return mapper;
        }

        Class<?> superType = exceptionType.getSuperclass();
        return Object.class.equals(superType) ? defaultMapper : getOrCreateMapperInHierarchy(superType);
    }
}
