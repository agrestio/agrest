package io.agrest.property;

@FunctionalInterface
public interface PropertyReader {

    Object value(Object root, String name);
}
