package io.agrest.sencha.it.fixture;

import io.agrest.it.fixture.CayenneAwareResponseAssertions;
import io.agrest.it.fixture.JerseyAndDerbyCase;

import javax.ws.rs.core.Response;

public abstract class SenchaBQJerseyTestOnDerby extends JerseyAndDerbyCase {

    @Override
    protected CayenneAwareResponseAssertions onResponse(Response response) {
        return new SenchaResponseAssertions(response, cayenneOpCounter);
    }
}
