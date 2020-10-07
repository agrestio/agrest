package io.agrest.unit;

import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * An integration test utility that is a DSL for executing Agrest requests and evaluating responses.
 */
public class AgHttpTester {

    private WebTarget target;

    public static AgHttpTester request(WebTarget target) {
        return new AgHttpTester(target);
    }

    protected AgHttpTester(WebTarget target) {
        this.target = Objects.requireNonNull(target);
    }

    public AgHttpTester path(String path) {
        this.target = target.path(path);
        return this;
    }

    public AgHttpTester queryParam(String name, Object... values) {
        Object[] encValues = Stream.of(values).map(AgHttpTester::urlEnc).toArray();
        this.target = target.queryParam(name, encValues);
        return this;
    }

    public AgHttpTester matrixParam(String name, Object... values) {
        Object[] encValues = Stream.of(values).map(AgHttpTester::urlEnc).toArray();
        this.target = target.matrixParam(name, encValues);
        return this;
    }

    public AgHttpResponseTester get() {
        return onResponse(this.request().get());
    }

    public AgHttpResponseTester put(String data) {
        Objects.requireNonNull(data);
        Response r = this.request().put(Entity.entity(data, MediaType.APPLICATION_JSON_TYPE));
        return onResponse(r);
    }

    public AgHttpResponseTester post(String data) {
        Objects.requireNonNull(data);
        Response r = this.request().post(Entity.entity(data, MediaType.APPLICATION_JSON_TYPE));
        return onResponse(r);
    }

    public AgHttpResponseTester delete() {
        return onResponse(this.request().delete());
    }

    public AgHttpResponseTester deleteWithEntity(String entity) {
        Response response = this.request()
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .method("DELETE", Entity.json(entity), Response.class);

        return onResponse(response);
    }

    protected static AgHttpResponseTester onResponse(Response response) {
        return new AgHttpResponseTester(response);
    }

    protected Invocation.Builder request() {
        return this.target.request();
    }

    private static String urlEnc(Object queryParam) {

        if (queryParam == null) {
            return "null";
        }

        try {
            // URLEncoder replaces spaces with "+"... Those are not decoded
            // properly by Jersey in 'uriInfo.getQueryParameters()' (TODO: why?)
            return URLEncoder.encode(queryParam.toString(), "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {

            // unexpected... we know that UTF-8 is present
            throw new RuntimeException(e);
        }
    }
}