package com.nhl.link.rest.it.sencha;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import com.nhl.link.rest.runtime.adapter.sencha.SenchaAdapter;

public class Sencha_PUT_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E3Resource.class);
	}

	@Override
	protected LinkRestBuilder doConfigure() {
		return super.doConfigure().adapter(new SenchaAdapter());
	}

	@Test
	public void testPut_ToOne_FromNull() throws WebApplicationException, IOException {

		newContext()
				.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		newContext()
				.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', null)"));

		E3 e3 = Cayenne.objectForPK(newContext(), E3.class, 3);
		newContext().invalidateObjects(e3);
		assertNull(e3.getE2());

		Response response1 = target("/e3/3").request()
				.put(Entity.entity("{\"id\":3,\"e2_id\":8}", MediaType.APPLICATION_JSON));
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
				response1.readEntity(String.class));

		e3 = Cayenne.objectForPK(newContext(), E3.class, 3);
		newContext().invalidateObjects(e3);
		assertEquals(8, Cayenne.intPKForObject(e3.getE2()));
	}

	@Test
	public void testPut_ToOne_ToNull() throws WebApplicationException, IOException {

		newContext()
				.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		newContext()
				.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', 8)"));

		E3 e3 = Cayenne.objectForPK(newContext(), E3.class, 3);
		newContext().invalidateObjects(e3);
		assertEquals(8, Cayenne.intPKForObject(e3.getE2()));

		Response response1 = target("/e3/3").request()
				.put(Entity.entity("{\"id\":3,\"e2_id\":null}", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
				response1.readEntity(String.class));

		e3 = Cayenne.objectForPK(newContext(), E3.class, 3);
		newContext().invalidateObjects(e3);
		assertNull(e3.getE2());
	}

	@Test
	public void testPut_ToOne() throws WebApplicationException, IOException {

		newContext()
				.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		newContext()
				.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', 8)"));

		E3 e3 = Cayenne.objectForPK(newContext(), E3.class, 3);
		newContext().invalidateObjects(e3);
		assertEquals(8, Cayenne.intPKForObject(e3.getE2()));

		Response response1 = target("/e3/3").request()
				.put(Entity.entity("{\"id\":3,\"e2_id\":1}", MediaType.APPLICATION_JSON));
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
				response1.readEntity(String.class));

		e3 = Cayenne.objectForPK(newContext(), E3.class, 3);
		newContext().invalidateObjects(e3);
		assertEquals(1, Cayenne.intPKForObject(e3.getE2()));
	}

	@Test
	public void testPut_ToOne_Relationship_Name() throws WebApplicationException, IOException {

		newContext()
				.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		newContext()
				.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', 8)"));

		Response response1 = target("/e3/3").request()
				.put(Entity.entity("{\"id\":3,\"e2\":1}", MediaType.APPLICATION_JSON));
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
				response1.readEntity(String.class));

		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id  = 1"));
	}
}
