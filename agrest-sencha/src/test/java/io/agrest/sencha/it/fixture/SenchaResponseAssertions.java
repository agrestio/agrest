package io.agrest.sencha.it.fixture;

import io.agrest.it.fixture.ResponseAssertions;

import javax.ws.rs.core.Response;

public class SenchaResponseAssertions extends ResponseAssertions {

    public SenchaResponseAssertions(Response response) {
        super(response);
    }

    @Override
    protected String buildExpectedJson(long total, String... jsonObjects) {
        String superJson = super.buildExpectedJson(total, jsonObjects);

        // TODO: what if  ___"success" : false___ is expected
        return "{\"success\":true," + superJson.substring(1);
    }

    // TODO: override "bodyEqualsMapBy"
}
