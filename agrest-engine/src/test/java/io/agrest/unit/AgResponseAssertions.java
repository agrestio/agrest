package io.agrest.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class AgResponseAssertions {

    private static final Pattern NUMERIC_ID_MATCHER = Pattern.compile("\"id\":([\\d]+)");

    private Response response;
    private String idPlaceholder;

    private String responseContent;

    public AgResponseAssertions(Response response) {
        this.response = response;
    }

    /**
     * Returns the first "id" field encoded in JSON.
     *
     * @return the first "id" field encoded in JSON or null
     */
    // TODO: since there may be many "id" fields in the hierarchy, it would probably make more sense to parse JSON
    //  and look for ID at the top level
    public Long getId() {
        Matcher matcher = NUMERIC_ID_MATCHER.matcher(getContentAsString());
        return matcher.find() ? Long.valueOf(matcher.group(1)) : null;
    }

    protected String buildExpectedJson(long total, String... jsonObjects) {
        StringBuilder expectedJson = new StringBuilder("{\"data\":[");

        if (jsonObjects.length > 0) {
            for (String o : jsonObjects) {
                expectedJson.append(o).append(",");
            }

            // remove last comma
            expectedJson.deleteCharAt(expectedJson.length() - 1);
        }

        expectedJson.append("],\"total\":")
                .append(total)
                .append("}");
        return expectedJson.toString();
    }

    public String getContentAsString() {
        // cache read content, as Response won't allow to read it twice..
        if (responseContent == null) {
            responseContent = response.readEntity(String.class);
        }

        return responseContent;
    }

    public AgResponseAssertions wasSuccess() {
        assertEquals(Response.Status.OK.getStatusCode(),
                response.getStatus(),
                "Failed request: " + response.getStatus());
        return this;
    }

    public AgResponseAssertions wasCreated() {
        assertEquals(Response.Status.CREATED.getStatusCode(),
                response.getStatus(),
                "Expected 'CREATED' status, was: " + response.getStatus());
        return this;
    }

    public AgResponseAssertions wasServerError() {
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                response.getStatus(),
                "Expected 'INTERNAL_SERVER_ERROR' status, was: " + response.getStatus());
        return this;
    }

    public AgResponseAssertions wasBadRequest() {
        return statusEquals(Response.Status.BAD_REQUEST);
    }

    public AgResponseAssertions wasNotFound() {
        return statusEquals(Response.Status.NOT_FOUND);
    }

    public AgResponseAssertions statusEquals(Response.Status expectedStatus) {
        assertEquals(expectedStatus.getStatusCode(), response.getStatus());
        return this;
    }

    public AgResponseAssertions mediaTypeEquals(MediaType expected) {
        assertEquals(expected, response.getMediaType());
        return this;
    }

    /**
     * Replaces id value in the actual result with a known placeholder, this allowing to compare JSON coming for
     * unknonw ids.
     */
    public AgResponseAssertions replaceId(String idPlaceholder) {
        this.idPlaceholder = idPlaceholder;
        return this;
    }

    public AgResponseAssertions bodyEquals(String expected) {
        String actual = getContentAsString();
        String normalized = idPlaceholder != null ? NUMERIC_ID_MATCHER.matcher(actual).replaceFirst("\"id\":" + idPlaceholder) : actual;

        assertEquals(expected, normalized, "Response contains unexpected JSON");
        return this;
    }

    public AgResponseAssertions bodyEquals(long total, String... jsonObjects) {
        return bodyEquals(buildExpectedJson(total, jsonObjects));
    }

    public AgResponseAssertions bodyEqualsMapBy(long total, String... jsonKeyValues) {

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

    public AgResponseAssertions totalEquals(long total) {

        String string = getContentAsString();
        JsonNode rootNode = null;
        try {
            rootNode = new ObjectMapper().readTree(string);
        } catch (IOException e) {
            throw new RuntimeException("Error reading JSON", e);
        }
        assertNotNull(rootNode, "No response data");

        JsonNode totalNode = rootNode.get("total");
        assertNotNull(totalNode, "No 'total' info");

        assertEquals(total, totalNode.asLong(), "Unexpected total");

        return this;
    }
}
