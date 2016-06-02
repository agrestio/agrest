package com.nhl.link.rest.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private Constraint constraint;


    private LinkRestClient(WebTarget target) {
        this.target = target;
        constraint = new Constraint();
    }

    public LinkRestClient exclude(String... excludePaths) {
        constraint.exclude(excludePaths);
        return this;
    }
    
    public LinkRestClient include(String... includePaths) {
        constraint.include(includePaths);
        return this;
    }

    public LinkRestClient include(Include include) {
        constraint.include(include);
        return this;
    }

    public LinkRestClient mapBy(String mapByPath) {
        constraint.mapBy(mapByPath);
        return this;
    }

    public LinkRestClient cayenneExp(Expression cayenneExp) {
        constraint.cayenneExp(cayenneExp);
        return this;
    }

    public LinkRestClient sort(String... properties) {
        constraint.sort(properties);
        return this;
    }

    public LinkRestClient sort(Sort ordering) {
        constraint.sort(ordering);
        return this;
    }

    public LinkRestClient start(long startIndex) {
        constraint.start(startIndex);
        return this;
    }

    public LinkRestClient limit(long limit) {
        constraint.limit(limit);
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

        LinkRestInvocation invocation = InvocationBuilder.target(target).constraint(constraint).build();
        Response response = invocation.invoke();

        DataResponseHandler<T> responseHandler = new DataResponseHandler<>(jsonFactory, jsonEntityReader);
        return responseHandler.handleResponse(response);
    }
}
