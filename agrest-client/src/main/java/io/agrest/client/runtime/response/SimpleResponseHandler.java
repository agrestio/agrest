package io.agrest.client.runtime.response;

import com.fasterxml.jackson.core.JsonFactory;
import io.agrest.client.ClientSimpleResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * @since 2.0
 */
public class SimpleResponseHandler extends BaseResponseHandler<ClientSimpleResponse> {

    public SimpleResponseHandler(JsonFactory jsonFactory) {
        super(jsonFactory);
    }

    @Override
    protected ClientSimpleResponse doHandleResponse(Status status, Response response) {
        return new ClientSimpleResponse(status);
    }
}
