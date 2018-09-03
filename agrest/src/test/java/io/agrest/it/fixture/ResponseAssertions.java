package io.agrest.it.fixture;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ResponseAssertions {

    private Response response;

    ResponseAssertions(Response response) {
        this.response = response;
    }

    public ResponseAssertions wasSuccess() {
        assertEquals("Failed request: " + response.getStatus(),
                Response.Status.OK.getStatusCode(),
                response.getStatus());
        return this;
    }

    public ResponseAssertions statusEquals(Response.Status expectedStatus) {
        assertEquals(
                expectedStatus.getStatusCode(),
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

    public ResponseAssertions bodyEqualsMapBy(long total, String... jsonKeyValues) {

        StringBuilder expectedJson = new StringBuilder("{\"data\":{");
        for (String o : jsonKeyValues) {
            expectedJson.append(o).append(",");
        }

        // rempve last comma
        expectedJson.deleteCharAt(expectedJson.length() - 1)
                .append("},\"total\":")
                .append(total)
                .append("}");

        return bodyEquals(expectedJson.toString());
    }

    public ResponseAssertions totalEquals(long total) {

        // Note: Response content type must be application/json
        JsonNode rootNode = response.readEntity(JsonNode.class);
        assertNotNull("No response data", rootNode);

        JsonNode totalNode = rootNode.get("total");
        assertNotNull("No 'total' info", totalNode);

        assertEquals("Unexpected total", total, totalNode.asLong());

        return this;
    }
}
