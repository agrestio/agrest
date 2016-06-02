package com.nhl.link.rest.client.runtime.response;

import javax.ws.rs.core.Response;

import com.nhl.link.rest.client.ClientSimpleResponse;

/**
 * @since 2.0
 */
@FunctionalInterface
public interface ClientResponseHandler<T extends ClientSimpleResponse> {

	T handleResponse(Response response);
}
