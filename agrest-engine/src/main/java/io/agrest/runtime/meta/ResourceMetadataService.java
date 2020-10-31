package io.agrest.runtime.meta;

import io.agrest.meta.AgResource;
import io.agrest.meta.parser.IResourceParser;
import org.apache.cayenne.di.Inject;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 1.18
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public class ResourceMetadataService implements IResourceMetadataService {

    private IResourceParser resourceParser;
    private ConcurrentMap<Class<?>, Collection<AgResource<?>>> classResources;
    private Optional<String> baseUrl;

    public ResourceMetadataService(@Inject IResourceParser resourceParser, @Inject BaseUrlProvider baseUrlProvider) {
        this.resourceParser = resourceParser;
        this.classResources = new ConcurrentHashMap<>();
        this.baseUrl = baseUrlProvider.getBaseUrl();
    }

    @Override
    public Collection<AgResource<?>> getAgResources(Class<?> resourceClass) {
        return classResources.computeIfAbsent(resourceClass, rc -> resourceParser.parse(rc));
    }

    @Override
    public Optional<String> getBaseUrl() {
        return baseUrl;
    }
}
