package io.agrest.jaxrs2.provider;

import io.agrest.AgResponse;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

/**
 * A JAX RS {@link DynamicFeature} that registers {@link ResponseStatusFilter} for resource methods returning
 * DataResponse.
 *
 * @since 1.1
 * @deprecated in favor of Jakarta version (JAX-RS 3)
 */
@Deprecated(since = "5.0", forRemoval = true)
public class ResponseStatusDynamicFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        // This check is important to not mess up responses modeled as anything other than DataResponse<T> or
        // SimpleResponse. This allows users to define their own statuses and headers outside Agrest
        if (AgResponse.class.isAssignableFrom(resourceInfo.getResourceMethod().getReturnType())) {
            context.register(ResponseStatusFilter.class);
        }
    }

}
