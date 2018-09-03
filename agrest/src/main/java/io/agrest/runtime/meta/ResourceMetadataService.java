package io.agrest.runtime.meta;

import io.agrest.meta.LrResource;
import io.agrest.meta.parser.IResourceParser;
import org.apache.cayenne.di.Inject;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 1.18
 */
public class ResourceMetadataService implements IResourceMetadataService {

    private IResourceParser resourceParser;
    private ConcurrentMap<Class<?>, Collection<LrResource<?>>> classResources;
    private Optional<String> baseUrl;

    public ResourceMetadataService(@Inject IResourceParser resourceParser, @Inject BaseUrlProvider baseUrlProvider) {
        this.resourceParser = resourceParser;
        this.classResources = new ConcurrentHashMap<>();
        this.baseUrl = baseUrlProvider.getBaseUrl();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
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

    @Override
    public Optional<String> getBaseUrl() {
        return baseUrl;
    }
}
