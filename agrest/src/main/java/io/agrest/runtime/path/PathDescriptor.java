package io.agrest.runtime.path;

public interface PathDescriptor<P> {

	boolean isAttribute();

	Class<?> getType();

	P getPathExp();
}
