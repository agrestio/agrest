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
