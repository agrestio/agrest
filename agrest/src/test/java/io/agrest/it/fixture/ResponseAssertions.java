package io.agrest.it.fixture;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.core.Response;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ResponseAssertions {

    private static Pattern ID_REPLACER = Pattern.compile("\"id\":[\\d]+");

    private Response response;
    private String idPlaceholder;

    public ResponseAssertions(Response response) {
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

    /**
     * Replaces id value in the actual result with a known placeholder, this allowing to compare JSON coming for
     * unknonw ids.
     */
    public ResponseAssertions replaceId(String idPlaceholder) {
        this.idPlaceholder = idPlaceholder;
        return this;
    }

    public ResponseAssertions bodyEquals(String expected) {
        String actual = response.readEntity(String.class);
        String normalized = idPlaceholder != null ? ID_REPLACER.matcher(actual).replaceFirst("\"id\":" + idPlaceholder) : actual;

        assertEquals("Response contains unexpected JSON", expected, normalized);
        return this;
    }

    public ResponseAssertions bodyEquals(long total, String... jsonObjects) {
        return bodyEquals(buildExpectedJson(total, jsonObjects));
    }

    protected String buildExpectedJson(long total, String... jsonObjects) {
        StringBuilder expectedJson = new StringBuilder("{\"data\":[");
        for (String o : jsonObjects) {
            expectedJson.append(o).append(",");
        }

        // remove last comma
        expectedJson.deleteCharAt(expectedJson.length() - 1)
                .append("],\"total\":")
                .append(total)
                .append("}");
        return expectedJson.toString();
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
