package com.nhl.link.rest.provider;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import com.nhl.link.rest.DataResponse;

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
		if (entity instanceof DataResponse) {

			@SuppressWarnings("rawtypes")
			DataResponse<?> drEntity = (DataResponse) entity;
			if (drEntity.getStatus() != null) {
				responseContext.setStatus(drEntity.getStatus().getStatusCode());
			}
		}
	}
}
