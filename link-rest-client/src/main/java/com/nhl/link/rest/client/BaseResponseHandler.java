package com.nhl.link.rest.client;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public abstract class BaseResponseHandler<T extends ClientSimpleResponse> implements ClientResponseHandler<T> {

    @Override
    public final T handleResponse(Response response) {

        Response.Status status = Response.Status.fromStatusCode(response.getStatus());
        if (status.getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new LinkRestClientException("Server returned " + status.name() + ": " + status.getReasonPhrase());
        }
        return doHandleResponse(status, response);
    }

    protected abstract T doHandleResponse(Status status, Response response);
}
