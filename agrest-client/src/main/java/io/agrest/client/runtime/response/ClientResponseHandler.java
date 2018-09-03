package io.agrest.client.runtime.response;

import javax.ws.rs.core.Response;

import io.agrest.client.ClientSimpleResponse;

/**
 * @since 2.0
 */
@FunctionalInterface
public interface ClientResponseHandler<T extends ClientSimpleResponse> {

	T handleResponse(Response response);
}
