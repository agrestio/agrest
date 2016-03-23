package com.nhl.link.rest.meta.parser;

import java.util.Collection;

import com.nhl.link.rest.meta.LrResource;

/**
 * @since 1.18
 */
public interface IResourceParser {

	<T> Collection<LrResource<?>> parse(Class<T> resourceClass);

}
