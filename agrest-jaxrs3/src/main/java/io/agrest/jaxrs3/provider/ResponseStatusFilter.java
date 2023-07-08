package io.agrest.jaxrs3.provider;

import io.agrest.AgResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;


/**
 * Ensures correct default response status for Agrest responses.
 *
 * @since 5.0
 */
@Provider
public class ResponseStatusFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        Object entity = responseContext.getEntity();
        if (entity instanceof AgResponse) {
            AgResponse response = (AgResponse) entity;
            responseContext.setStatus(response.getStatus());
        }
    }
}
