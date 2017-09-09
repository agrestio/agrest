package com.nhl.link.rest.it.fixture;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class ResponseAssertions {

    private Response response;

    ResponseAssertions(Response response) {
        this.response = response;
    }

    public ResponseAssertions assertSuccess() {
        assertEquals("Failed request: " + response.getStatus(),
                Response.Status.OK.getStatusCode(),
                response.getStatus());
        return this;
    }

    public ResponseAssertions bodyEquals(String expected) {
        assertEquals("Response contains unexpected JSON", expected, response.readEntity(String.class));
        return this;
    }

    public ResponseAssertions bodyEquals(long total, String... jsonObjects) {

        StringBuilder expectedJson = new StringBuilder("{\"data\":[");
        for (String o : jsonObjects) {
            expectedJson.append(o).append(",");
        }

        // rempve last comma
        expectedJson.deleteCharAt(expectedJson.length() - 1)
                .append("],\"total\":")
                .append(total)
                .append("}");

        return bodyEquals(expectedJson.toString());
    }
}
