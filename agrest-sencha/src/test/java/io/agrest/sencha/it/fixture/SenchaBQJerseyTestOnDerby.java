package io.agrest.sencha.it.fixture;

import io.agrest.it.fixture.BQJerseyTestOnDerby;
import io.agrest.it.fixture.ResponseAssertions;

import javax.ws.rs.core.Response;

public abstract class SenchaBQJerseyTestOnDerby extends BQJerseyTestOnDerby {

    @Override
    protected ResponseAssertions onResponse(Response response) {
        return new SenchaResponseAssertions(response);
    }
}
