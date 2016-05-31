package com.nhl.link.rest.client;

import com.fasterxml.jackson.core.JsonFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class SimpleResponseHandler extends BaseResponseHandler<ClientSimpleResponse> {

    SimpleResponseHandler(JsonFactory jsonFactory) {
        super(jsonFactory);
    }

    @Override
    protected ClientSimpleResponse doHandleResponse(Status status, Response response) {
        return new ClientSimpleResponse(status);
    }
}
