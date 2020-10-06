package io.agrest.unit;

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
 * A DSL for evaluating Agrest responses.
 */
public class AgTester {

    private WebTarget target;

    public static AgTester request(WebTarget target) {
        return new AgTester(target);
    }

    protected AgTester(WebTarget target) {
        this.target = Objects.requireNonNull(target);
    }

    public AgTester path(String path) {
        this.target = target.path(path);
        return this;
    }

    public AgTester queryParam(String name, Object... values) {
        Object[] encValues = Stream.of(values).map(AgTester::urlEnc).toArray();
        this.target = target.queryParam(name, encValues);
        return this;
    }

    public AgTester matrixParam(String name, Object... values) {
        Object[] encValues = Stream.of(values).map(AgTester::urlEnc).toArray();
        this.target = target.matrixParam(name, encValues);
        return this;
    }

    public AgResponseAssertions get() {
        return onResponse(this.request().get());
    }

    public AgResponseAssertions put(String data) {
        Objects.requireNonNull(data);
        Response r = this.request().put(Entity.entity(data, MediaType.APPLICATION_JSON_TYPE));
        return onResponse(r);
    }

    public AgResponseAssertions post(String data) {
        Objects.requireNonNull(data);
        Response r = this.request().post(Entity.entity(data, MediaType.APPLICATION_JSON_TYPE));
        return onResponse(r);
    }

    public AgResponseAssertions delete() {
        return onResponse(this.request().delete());
    }

    protected static AgResponseAssertions onResponse(Response response) {
        return new AgResponseAssertions(response);
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