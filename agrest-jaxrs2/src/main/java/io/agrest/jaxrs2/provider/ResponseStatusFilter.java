package io.agrest.jaxrs2.provider;

import io.agrest.AgResponse;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Ensures correct default response status for Agrest responses.
 *
 * @since 1.1
 * @deprecated in favor of Jakarta version (JAX-RS 3)
 */
@Deprecated(since = "5.0", forRemoval = true)
@Provider
public class ResponseStatusFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object entity = responseContext.getEntity();
        if (entity instanceof AgResponse) {
            AgResponse response = (AgResponse) entity;
            responseContext.setStatus(response.getStatus());
            response.getHeaders().forEach((n, h) -> responseContext.getHeaders().put(n, h));
        }
    }
}
