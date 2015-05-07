package com.nhl.link.rest.runtime.meta;

import com.nhl.link.rest.meta.LrResource;
import com.nhl.link.rest.meta.parser.IResourceParser;
import org.apache.cayenne.di.Inject;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ResourceMetadataService implements IResourceMetadataService {

    private IResourceParser resourceParser;
	private ConcurrentMap<Class, Collection<LrResource>> classResources;

    public ResourceMetadataService(@Inject IResourceParser resourceParser) {
        this.resourceParser = resourceParser;
		this.classResources = new ConcurrentHashMap<>();
    }

    @Override
	public Collection<LrResource> getLrResources(Class<?> resourceClass) {

		Collection<LrResource> resources = classResources.get(resourceClass);
		if (resources == null) {
			Collection<LrResource> newResources = resourceParser.parse(resourceClass);

			Collection<LrResource> existingResources = classResources.putIfAbsent(resourceClass, newResources);
			resources = existingResources == null? newResources : existingResources;
		}

		return resources;
	}
}
