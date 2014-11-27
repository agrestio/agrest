package com.nhl.link.rest.it.noadapter;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.E12Resource;
import com.nhl.link.rest.it.fixture.E2Resource;
import com.nhl.link.rest.it.fixture.E3Resource;
import com.nhl.link.rest.it.fixture.cayenne.E12;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.unit.JerseyTestOnDerby;

public class GET_Related_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E2Resource.class);
		context.register(E3Resource.class);
		context.register(E12Resource.class);
	}

	@Test
	public void testGet_ToMany_Constrained() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));

		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (7, 2, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response r1 = target("/e2/constraints/1/e3s").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}", r1.readEntity(String.class));
	}

	@Test
	public void testGet_ValidRel_ToMany() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (7, 2, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response r1 = target("/e2/1/e3s").queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}", r1.readEntity(String.class));
	}

	@Test
	public void testGet_ValidRel_ToOne() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (7, 2, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response r1 = target("/e3/7/e2").queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":2}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void testGet_InvalidRel() {
		Response r1 = target("/e2/1/dummyrel").request().get();

		assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"Invalid relationship: 'dummyrel'\"}",
				r1.readEntity(String.class));
	}

	@Test
	public void testGET_ToManyJoin() {

		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e12 (id) values (11)"));
		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e12 (id) values (12)"));
		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (14)"));
		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (15)"));
		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (16)"));

		context.performGenericQuery(new SQLTemplate(E12.class,
				"INSERT INTO utest.e12_e13 (e12_id, e13_id) values (11, 14)"));
		context.performGenericQuery(new SQLTemplate(E12.class,
				"INSERT INTO utest.e12_e13 (e12_id, e13_id) values (12, 16)"));

		// excluding ID - can't render multi-column IDs yet
		Response r1 = target("/e12/12/e1213").queryParam("exclude", "id").queryParam("include", "e12")
				.queryParam("include", "e13").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"e12\":{\"id\":12},\"e13\":{\"id\":16}}],\"total\":1}",
				r1.readEntity(String.class));
	}
}
