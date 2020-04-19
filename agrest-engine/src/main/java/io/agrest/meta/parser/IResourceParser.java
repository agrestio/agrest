package io.agrest.meta.parser;

import java.util.Collection;

import io.agrest.meta.AgResource;

/**
 * @since 1.18
 */
public interface IResourceParser {

	<T> Collection<AgResource<?>> parse(Class<T> resourceClass);

}
