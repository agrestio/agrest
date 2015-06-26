package com.nhl.link.rest.runtime.meta;

import com.nhl.link.rest.meta.LrResource;

import java.util.Collection;

/**
 * Provides access to LinkRest resource metadata.
 * 
 * @since 1.18
 */
public interface IResourceMetadataService {

	Collection<LrResource<?>> getLrResources(Class<?> resourceClass);
}
