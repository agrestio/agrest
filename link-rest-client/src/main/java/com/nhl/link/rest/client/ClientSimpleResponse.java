package com.nhl.link.rest.client;

import javax.ws.rs.core.Response.Status;

public class ClientSimpleResponse {

    private Status status;

    public ClientSimpleResponse(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
