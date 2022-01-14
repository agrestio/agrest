package io.agrest.spi;

import io.agrest.AgException;

/**
 * Agrest service provider interface that allows an extension to map its own exceptions to AgException.
 *
 * @since 5.0
 */
public interface AgExceptionMapper<E extends Throwable> {

    AgException toAgException(E e);
}
