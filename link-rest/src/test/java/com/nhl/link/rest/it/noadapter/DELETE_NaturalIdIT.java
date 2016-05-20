package com.nhl.link.rest.it.noadapter;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E20;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.resource.E20Resource;
import com.nhl.link.rest.it.fixture.resource.E21Resource;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DELETE_NaturalIdIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E20Resource.class);
		context.register(E21Resource.class);
    }

    @Test
	public void testDelete() throws IOException {

		insert("e20", "name", "'John'");
		insert("e20", "name", "'Brian'");

		assertEquals(2L, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E20 a")));

		Response response1 = target("/e20/John").request().delete();
		assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true}", response1.readEntity(String.class));

		assertEquals(1L, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E20 a WHERE a.name = 'Brian'")));
	}

	@Test
	public void testDelete_MultiId() throws IOException {

		insert("e21", "age, name", "18, 'John'");
		insert("e21", "age, name", "27, 'Brian'");

		assertEquals(2L, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E21 a")));

		Response response1 = target("/e21/byid").queryParam("age", 18).queryParam("name", "John")
				.request().delete();
		assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true}", response1.readEntity(String.class));

		assertEquals(1L, Cayenne.objectForQuery(newContext(), new EJBQLQuery(
				"select count(a) from E21 a WHERE a.age = 27 and a.name = 'Brian'")));
	}
}
