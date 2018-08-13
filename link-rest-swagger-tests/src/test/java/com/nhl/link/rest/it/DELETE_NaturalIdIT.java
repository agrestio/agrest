package com.nhl.link.rest.it;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E21;
import com.nhl.link.rest.swagger.api.v1.service.E20Resource;
import com.nhl.link.rest.swagger.api.v1.service.E21Resource;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.EJBQLQuery;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class DELETE_NaturalIdIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E20Resource.class);
        context.register(E21Resource.class);
    }

    @Test
    public void testDelete_SingleId() {

        insert("e20", "name", "'John'");
        insert("e20", "name", "'Brian'");

        Response response = target("/v1/e20/John").request().delete();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":true}", response.readEntity(String.class));

        assertEquals(1L, Cayenne.objectForQuery(newContext(),
                new EJBQLQuery("select count(a) from E20 a WHERE a.name = 'Brian'")));
    }

    @Test
    public void testDelete_MultiId() {

        insert("e21", "age, name", "18, 'John'");
        insert("e21", "age, name", "27, 'Brian'");

        Response response = target("/v1/e21")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .request()
                .delete();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":true}", response.readEntity(String.class));

        assertEquals(1L, countRows(E21.class, E21.AGE.eq(27).andExp(E21.NAME.eq("Brian"))));
    }
}
