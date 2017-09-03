package com.nhl.link.rest.runtime.meta;

import com.nhl.link.rest.meta.LrResource;

import java.util.Collection;
import java.util.Optional;

/**
 * Provides access to LinkRest resource metadata.
 *
 * @since 1.18
 */
public interface IResourceMetadataService {

    Collection<LrResource<?>> getLrResources(Class<?> resourceClass);

    /**
     * Returns base URL of this REST service, if it was set when LinkRest was started.
     *
     * @return a base URL of this REST service.
     * @since 2.10
     */
    Optional<String> getBaseUrl();
}
