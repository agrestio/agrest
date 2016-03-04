package com.nhl.link.rest.provider;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import com.nhl.link.rest.LrResponse;

/**
 * Ensures correct default response status for LinkRest responses.
 * 
 * @since 1.1
 */
@Provider
public class ResponseStatusFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		Object entity = responseContext.getEntity();
		if (entity instanceof LrResponse) {

			LrResponse response = (LrResponse) entity;
			if (response.getStatus() != null) {
				responseContext.setStatus(response.getStatus().getStatusCode());
			}
		}
	}
}
