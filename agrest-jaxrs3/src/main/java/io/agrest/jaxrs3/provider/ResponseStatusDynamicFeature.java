package io.agrest.jaxrs3.provider;

import io.agrest.AgResponse;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;

/**
 * A JAX RS {@link DynamicFeature} that registers {@link ResponseStatusFilter} for resource methods returning
 * DataResponse.
 *
 * @since 5.0
 */
public class ResponseStatusDynamicFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        // ignore any other method signatures - in those cases we assume
        // response status is set by the user on their own..
        if (AgResponse.class.isAssignableFrom(resourceInfo.getResourceMethod().getReturnType())) {
            context.register(ResponseStatusFilter.class);
        }
    }

}
