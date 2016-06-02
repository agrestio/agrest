package com.nhl.link.rest.client;

import javax.ws.rs.core.Response;

/**
 * @since 2.0
 */
@FunctionalInterface
public interface ClientResponseHandler<T extends ClientSimpleResponse> {

	T handleResponse(Response response);
}
