package com.nhl.link.rest.runtime.meta;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.meta.LrResource;
import com.nhl.link.rest.meta.parser.IResourceParser;

/**
 * @since 1.18
 */
public class ResourceMetadataService implements IResourceMetadataService {

	private IResourceParser resourceParser;
	private ConcurrentMap<Class<?>, Collection<LrResource<?>>> classResources;

	public ResourceMetadataService(@Inject IResourceParser resourceParser) {
		this.resourceParser = resourceParser;
		this.classResources = new ConcurrentHashMap<>();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Collection<LrResource<?>> getLrResources(Class<?> resourceClass) {

		Collection resources = classResources.get(resourceClass);
		if (resources == null) {
			Collection newResources = resourceParser.parse(resourceClass);

			Collection existingResources = classResources.putIfAbsent(resourceClass, newResources);
			resources = existingResources == null ? newResources : existingResources;
		}

		return resources;
	}
}
