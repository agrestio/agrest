package io.agrest.it.fixture;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AgTester {

    private WebTarget target;
    private Consumer<Invocation.Builder> requestCustomizer;

    public static AgTester request(WebTarget target) {
        return new AgTester(target);
    }

    protected AgTester(WebTarget target) {
        this.target = Objects.requireNonNull(target);
        this.requestCustomizer = (b) -> {
        };
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

    public AgTester customizeRequest(Consumer<Invocation.Builder> requestCustomizer) {
        this.requestCustomizer = Objects.requireNonNull(requestCustomizer);
        return this;
    }

    public ResponseAssertions get() {
        return onResponse(this.request().get());
    }

    public ResponseAssertions put(String data) {
        Objects.requireNonNull(data);
        Response r = this.request().put(Entity.entity(data, MediaType.APPLICATION_JSON_TYPE));
        return onResponse(r);
    }

    public ResponseAssertions post(String data) {
        Objects.requireNonNull(data);
        Response r = this.request().post(Entity.entity(data, MediaType.APPLICATION_JSON_TYPE));
        return onResponse(r);
    }

    public ResponseAssertions delete() {
        return onResponse(this.request().delete());
    }

    protected static ResponseAssertions onResponse(Response response) {
        return new ResponseAssertions(response);
    }

    protected Invocation.Builder request() {
        Invocation.Builder requestBuilder = this.target.request();
        this.requestCustomizer.accept(requestBuilder);
        return requestBuilder;
    }

    private static String urlEnc(Object queryParam) {

        if(queryParam == null) {
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