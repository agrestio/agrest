package com.nhl.link.rest.it.noadapter;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.resource.E4Resource;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

/**
 * @deprecated since 2.10, as the API being tested was deprecated.
 */
public class GET_Props_TransientProperty_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E4Resource.class);
    }

    @Override
    protected LinkRestBuilder doConfigure() {
        return super.doConfigure().transientProperty(E4.class, "derived");
    }

    @Test
    public void testTransientAttribute() {

        insert("e4", "id, c_varchar", "1, 'x'");
        insert("e4", "id, c_varchar", "2, 'y'");

        Response r = target("/e4")
                .queryParam("include", "derived")
                .queryParam("sort", "id")
                .request()
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"derived\":\"x$\"},{\"derived\":\"y$\"}],\"total\":2}",
                r.readEntity(String.class));
    }
}
