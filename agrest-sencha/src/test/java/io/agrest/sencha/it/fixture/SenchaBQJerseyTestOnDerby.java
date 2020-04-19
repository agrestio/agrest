package io.agrest.sencha.it.fixture;

import io.agrest.cayenne.unit.CayenneAwareResponseAssertions;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;

import javax.ws.rs.core.Response;

public abstract class SenchaBQJerseyTestOnDerby extends JerseyAndDerbyCase {

    @Override
    protected CayenneAwareResponseAssertions onResponse(Response response) {
        return new SenchaResponseAssertions(response, cayenneOpCounter);
    }
}
