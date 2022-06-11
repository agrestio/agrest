package io.agrest.reader;

/**
 * @since 5.0
 */
@FunctionalInterface
public interface DataReader {

    Object read(Object object);
}
