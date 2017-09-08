package com.nhl.link.rest.it;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.resource.E4Resource;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;

public class GET_Props_EntityProperty_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E4Resource.class);
    }

    @Test
    public void testRequestEntityProperty() {

        newContext().performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) values (1), (2)"));

        Response r = target("/e4/calc_property")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .request()
                .get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"x\":\"y_1\"},{\"id\":2,\"x\":\"y_2\"}],\"total\":2}",
                r.readEntity(String.class));
    }
}
