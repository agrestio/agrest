package io.agrest.backend.util.converter;

/**
 *
 *
 */
@FunctionalInterface
public interface Converter<A, B> {

    B convert(A from);

}
