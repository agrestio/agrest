package io.agrest.runtime;

import io.agrest.AgException;
import io.agrest.spi.AgExceptionMapper;
import org.apache.cayenne.di.Inject;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 5.0
 */
public class ExceptionHandler {

    private final ConcurrentMap<Class<? extends Throwable>, AgExceptionMapper<?>> exceptionMappers;
    private final AgExceptionMapper<?> defaultMapper;

    public ExceptionHandler(@Inject Map<Class<? extends Throwable>, AgExceptionMapper<?>> exceptionMappers) {
        // make a copy, as we'll be expanding the map dynamically with known exception subclasses
        this.exceptionMappers = new ConcurrentHashMap<>(exceptionMappers);

        // hide exception details from the client
        this.defaultMapper = th -> AgException.internalServerError(th, "Exception processing Agrest request");
    }

    public <T> T runAndCatchExceptions(Callable<T> action) throws AgException {
        try {
            return action.call();
        } catch (AgException e) {
            throw e;
        } catch (Throwable th) {
            AgExceptionMapper mapper = locateMapper(th.getClass());
            throw mapper.toAgException(th);
        }
    }

    private AgExceptionMapper<?> locateMapper(Class<?> exceptionType) {

        AgExceptionMapper<?> mapper = exceptionMappers.get(exceptionType);
        if (mapper != null) {
            return mapper;
        }

        Class<?> superType = exceptionType.getClass().getSuperclass();
        return Object.class.equals(superType) ? defaultMapper : locateMapper(superType);

    }
}
