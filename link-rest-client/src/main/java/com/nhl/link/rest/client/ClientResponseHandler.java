package com.nhl.link.rest.client;

import javax.ws.rs.core.Response;

@FunctionalInterface
public interface ClientResponseHandler<T extends ClientSimpleResponse> {

    T handleResponse(Response response);
}
