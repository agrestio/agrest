package io.agrest.sencha.unit;

import io.agrest.cayenne.unit.CayenneAwareResponseAssertions;
import io.agrest.cayenne.unit.CayenneOpCounter;

import javax.ws.rs.core.Response;

public class SenchaResponseAssertions extends CayenneAwareResponseAssertions {

    public SenchaResponseAssertions(Response response, CayenneOpCounter opCounter) {
        super(response, opCounter);
    }

    @Override
    protected String buildExpectedJson(long total, String... jsonObjects) {
        String superJson = super.buildExpectedJson(total, jsonObjects);

        // TODO: what if  ___"success" : false___ is expected
        return "{\"success\":true," + superJson.substring(1);
    }

    // TODO: override "bodyEqualsMapBy"
}
