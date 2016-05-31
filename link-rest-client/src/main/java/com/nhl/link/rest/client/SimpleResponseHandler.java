package com.nhl.link.rest.client;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class SimpleResponseHandler extends BaseResponseHandler<ClientSimpleResponse> {

    @Override
    protected ClientSimpleResponse doHandleResponse(Status status, Response response) {
        return new ClientSimpleResponse(status);
    }
}
