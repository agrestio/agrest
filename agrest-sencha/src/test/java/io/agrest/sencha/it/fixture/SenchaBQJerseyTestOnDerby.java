package io.agrest.sencha.it.fixture;

import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.ResponseAssertions;

import javax.ws.rs.core.Response;

public abstract class SenchaBQJerseyTestOnDerby extends JerseyAndDerbyCase {

    @Override
    protected ResponseAssertions onResponse(Response response) {
        return new SenchaResponseAssertions(response);
    }
}
