package io.agrest.client;

import javax.ws.rs.core.Response.Status;

/**
 * @since 2.0
 */
public class ClientSimpleResponse {

    private Status status;

    public ClientSimpleResponse(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
