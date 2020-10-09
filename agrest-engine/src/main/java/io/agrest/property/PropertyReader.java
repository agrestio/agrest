package io.agrest.property;

@FunctionalInterface
public interface PropertyReader {

    /**
     * @since 3.7
     */
    Object value(Object object);
}
