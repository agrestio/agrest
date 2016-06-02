package com.nhl.link.rest.client;

import javax.ws.rs.core.Response.Status;
import java.util.List;

public class ClientDataResponse<T> extends ClientSimpleResponse {

    private List<T> data;
    private long total;

    public ClientDataResponse(Status status, List<T> data, long total) {
        super(status);
        this.data = data;
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public long getTotal() {
        return total;
    }
}
