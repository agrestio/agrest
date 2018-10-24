package io.agrest.runtime.meta;

import io.agrest.meta.AgResource;

import java.util.Collection;
import java.util.Optional;

/**
 * Provides access to Agrest resource metadata.
 *
 * @since 1.18
 */
public interface IResourceMetadataService {

    Collection<AgResource<?>> getAgResources(Class<?> resourceClass);

    /**
     * Returns base URL of this REST service, if it was set when Agrest was started.
     *
     * @return a base URL of this REST service.
     * @since 2.10
     */
    Optional<String> getBaseUrl();
}
