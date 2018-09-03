package io.agrest.meta.parser;

import java.util.Collection;

import io.agrest.meta.LrResource;

/**
 * @since 1.18
 */
public interface IResourceParser {

	<T> Collection<LrResource<?>> parse(Class<T> resourceClass);

}
