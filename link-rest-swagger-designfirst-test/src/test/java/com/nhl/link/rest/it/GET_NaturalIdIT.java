package com.nhl.link.rest.it;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.swagger.api.v1.service.E20Resource;
import com.nhl.link.rest.swagger.api.v1.service.E21Resource;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class GET_NaturalIdIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E20Resource.class);
        context.register(E21Resource.class);
    }

    @Test
    public void test_SelectById() {

        newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e20 (name) values ('John')"));

        Response response1 = target("/v1/e20/John").queryParam("exclude", "age", "description").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":\"John\",\"name\":\"John\"}],\"total\":1}",
                response1.readEntity(String.class));

        newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e20 (name) values ('John')"));

        Response response2 = target("/v1/e20/John").queryParam("exclude", "age", "description").request().get();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID 'John' and entity 'E20'\"}",
                response2.readEntity(String.class));
    }

    @Test
    public void test_SelectById_MultiId() {

        newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e21 (age, name) values (18, 'John')"));

        Response response1 = target("/v1/e21")
                .queryParam("age", 18).queryParam("name", "John")
                .queryParam("exclude", "description").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}],\"total\":1}",
                response1.readEntity(String.class));

        newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e21 (age, name) values (18, 'John')"));

        Response response2 = target("/v1/e21")
                .queryParam("age", 18).queryParam("name", "John")
                .queryParam("exclude", "description").request().get();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID '{name:John,age:18}' and entity 'E21'\"}",
                response2.readEntity(String.class));
    }
}
