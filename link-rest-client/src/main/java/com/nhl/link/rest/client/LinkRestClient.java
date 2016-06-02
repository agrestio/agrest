package com.nhl.link.rest.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.link.rest.client.protocol.LrRequest;
import com.nhl.link.rest.client.protocol.Include;
import com.nhl.link.rest.client.protocol.Sort;
import com.nhl.link.rest.client.runtime.jackson.IJsonEntityReader;
import com.nhl.link.rest.client.runtime.jackson.IJsonEntityReaderFactory;
import com.nhl.link.rest.client.runtime.jackson.JsonEntityReaderFactory;
import com.nhl.link.rest.client.runtime.response.DataResponseHandler;
import com.nhl.link.rest.client.runtime.run.InvocationBuilder;
import com.nhl.link.rest.client.runtime.run.LinkRestInvocation;

import org.apache.cayenne.exp.Expression;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * @since 2.0
 */
public class LinkRestClient {

    private static JsonFactory jsonFactory;
    private static IJsonEntityReaderFactory jsonEntityReaderFactory;

    static {
        jsonFactory = new ObjectMapper().getFactory();
        jsonEntityReaderFactory = new JsonEntityReaderFactory();
    }

    public static LinkRestClient client(WebTarget target) {
        return new LinkRestClient(target);
    }

    private WebTarget target;
    private LrRequest request;


    private LinkRestClient(WebTarget target) {
        this.target = target;
        request = new LrRequest();
    }

    public LinkRestClient exclude(String... excludePaths) {
        request.exclude(excludePaths);
        return this;
    }
    
    public LinkRestClient include(String... includePaths) {
        request.include(includePaths);
        return this;
    }

    public LinkRestClient include(Include include) {
        request.include(include);
        return this;
    }

    public LinkRestClient mapBy(String mapByPath) {
        request.mapBy(mapByPath);
        return this;
    }

    public LinkRestClient cayenneExp(Expression cayenneExp) {
        request.cayenneExp(cayenneExp);
        return this;
    }

    public LinkRestClient sort(String... properties) {
        request.sort(properties);
        return this;
    }

    public LinkRestClient sort(Sort ordering) {
        request.sort(ordering);
        return this;
    }

    public LinkRestClient start(long startIndex) {
        request.start(startIndex);
        return this;
    }

    public LinkRestClient limit(long limit) {
        request.limit(limit);
        return this;
    }

    public <T> ClientDataResponse<T> get(Class<T> targetType) {

        if (targetType == null) {
            throw new NullPointerException("Target type");
        }

        IJsonEntityReader<T> jsonEntityReader = jsonEntityReaderFactory.getReaderForType(targetType);
        if (jsonEntityReader == null) {
            throw new LinkRestClientException("Unsupported target type: " + targetType.getName());
        }

        LinkRestInvocation invocation = InvocationBuilder.target(target).constraint(request).build();
        Response response = invocation.invoke();

        DataResponseHandler<T> responseHandler = new DataResponseHandler<>(jsonFactory, jsonEntityReader);
        return responseHandler.handleResponse(response);
    }
}
