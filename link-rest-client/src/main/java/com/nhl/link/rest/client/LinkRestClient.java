package com.nhl.link.rest.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.cayenne.exp.Expression;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

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
    private ConstraintBuilder constraintBuilder;


    private LinkRestClient(WebTarget target) {
        this.target = target;
        constraintBuilder = new ConstraintBuilder();
    }

    public LinkRestClient exclude(String... excludePaths) {
        constraintBuilder.exclude(excludePaths);
        return this;
    }
    
    public LinkRestClient include(String... includePaths) {
        constraintBuilder.include(includePaths);
        return this;
    }

    public LinkRestClient include(Include include) {
        constraintBuilder.include(include);
        return this;
    }

    public LinkRestClient mapBy(String mapByPath) {
        constraintBuilder.mapBy(mapByPath);
        return this;
    }

    public LinkRestClient cayenneExp(Expression cayenneExp) {
        constraintBuilder.cayenneExp(cayenneExp);
        return this;
    }

    public LinkRestClient sort(String... properties) {
        constraintBuilder.sort(properties);
        return this;
    }

    public LinkRestClient sort(Sort ordering) {
        constraintBuilder.sort(ordering);
        return this;
    }

    public LinkRestClient start(long startIndex) {
        constraintBuilder.start(startIndex);
        return this;
    }

    public LinkRestClient limit(long limit) {
        constraintBuilder.limit(limit);
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

        LinkRestInvocation invocation = InvocationBuilder.target(target).constraint(constraintBuilder.build()).build();
        Response response = invocation.invoke();

        DataResponseHandler<T> responseHandler = new DataResponseHandler<>(jsonFactory, jsonEntityReader);
        return responseHandler.handleResponse(response);
    }
}
