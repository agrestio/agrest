package com.nhl.link.rest.it.noadapter;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.it.fixture.cayenne.E17;
import com.nhl.link.rest.it.fixture.cayenne.E18;
import com.nhl.link.rest.it.fixture.resource.E17Resource;
import com.nhl.link.rest.it.fixture.resource.E18Resource;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E12;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.resource.E12Resource;
import com.nhl.link.rest.it.fixture.resource.E2Resource;
import com.nhl.link.rest.it.fixture.resource.E3Resource;

public class GET_Related_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E2Resource.class);
		context.register(E3Resource.class);
		context.register(E12Resource.class);
		context.register(E17Resource.class);
		context.register(E18Resource.class);
	}

	@Test
	public void testGet_ToMany_Constrained() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));

		performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (7, 2, 'zzz')"));
		performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response r1 = target("/e2/constraints/1/e3s").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}", r1.readEntity(String.class));
	}

	@Test
	public void testGet_ToMany_CompoundId() {

		performQuery(new SQLTemplate(E17.class, "INSERT INTO utest.e17 (id1, id2, name) values (1, 1, 'aaa')"));
		performQuery(new SQLTemplate(E17.class, "INSERT INTO utest.e17 (id1, id2, name) values (2, 2, 'bbb')"));

		performQuery(new SQLTemplate(E18.class,
				"INSERT INTO utest.e18 (id, e17_id1, e17_id2, name) values (1, 1, 1, 'xxx')"));
		performQuery(new SQLTemplate(E18.class,
				"INSERT INTO utest.e18 (id, e17_id1, e17_id2, name) values (2, 1, 1, 'yyy')"));
		performQuery(new SQLTemplate(E18.class,
				"INSERT INTO utest.e18 (id, e17_id1, e17_id2, name) values (3, 2, 2, 'zzz')"));

		Response r1 = target("/e17/e18s").matrixParam("parentId1", 1).matrixParam("parentId2", 1).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":1,\"name\":\"xxx\"},{\"id\":2,\"name\":\"yyy\"}],\"total\":2}",
				r1.readEntity(String.class));
	}

	@Test
	public void testGet_ValidRel_ToOne_CompoundId() {

		performQuery(new SQLTemplate(E17.class, "INSERT INTO utest.e17 (id1, id2, name) values (1, 1, 'aaa')"));
		performQuery(new SQLTemplate(E17.class, "INSERT INTO utest.e17 (id1, id2, name) values (2, 2, 'bbb')"));

		performQuery(new SQLTemplate(E18.class,
				"INSERT INTO utest.e18 (id, e17_id1, e17_id2, name) values (1, 1, 1, 'xxx')"));
		performQuery(new SQLTemplate(E18.class,
				"INSERT INTO utest.e18 (id, e17_id1, e17_id2, name) values (2, 1, 1, 'yyy')"));
		performQuery(new SQLTemplate(E18.class,
				"INSERT INTO utest.e18 (id, e17_id1, e17_id2, name) values (3, 2, 2, 'zzz')"));

		Response r1 = target("/e18/1").queryParam("include", E18.E17.getName()).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals(
				"{\"data\":[{\"id\":1," + "\"e17\":{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"aaa\"},"
						+ "\"name\":\"xxx\"}],\"total\":1}",
				r1.readEntity(String.class));
	}

	@Test
	public void testGet_CompoundId_UnmappedPk() {

		// remove a part of PK from the ObjEntity
		DataMap dataMap = DB_STACK.getCayenneStack().getChannel().getEntityResolver().getDataMap("datamap");
		ObjEntity E17 = dataMap.getObjEntity("E17");
		ObjAttribute unmappedAttribute = E17.getAttribute("id2");
		E17.removeAttribute("id2");

		performQuery(new SQLTemplate(E17.class, "INSERT INTO utest.e17 (id1, id2, name) values (1, 1, 'aaa')"));
		performQuery(new SQLTemplate(E17.class, "INSERT INTO utest.e17 (id1, id2, name) values (2, 2, 'bbb')"));

		performQuery(new SQLTemplate(E18.class,
				"INSERT INTO utest.e18 (id, e17_id1, e17_id2, name) values (1, 1, 1, 'xxx')"));
		performQuery(new SQLTemplate(E18.class,
				"INSERT INTO utest.e18 (id, e17_id1, e17_id2, name) values (2, 1, 1, 'yyy')"));
		performQuery(new SQLTemplate(E18.class,
				"INSERT INTO utest.e18 (id, e17_id1, e17_id2, name) values (3, 2, 2, 'zzz')"));

		Response r1 = target("/e18/1").queryParam("include", E18.E17.getName()).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":1,\"" + "e17\":{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"name\":\"aaa\"},"
				+ "\"name\":\"xxx\"}],\"total\":1}", r1.readEntity(String.class));

		// restore initial state
		E17.addAttribute(unmappedAttribute);
	}

	@Test
	public void testGet_ValidRel_ToMany() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));
		performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (7, 2, 'zzz')"));
		performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response r1 = target("/e2/1/e3s").queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}", r1.readEntity(String.class));
	}

	@Test
	public void testGet_ValidRel_ToOne() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		performQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));
		performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (7, 2, 'zzz')"));
		performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		performQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response r1 = target("/e3/7/e2").queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", r1.readEntity(String.class));
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

		performQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e12 (id) values (11)"));
		performQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e12 (id) values (12)"));
		performQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (14)"));
		performQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (15)"));
		performQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (16)"));

		performQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e12_e13 (e12_id, e13_id) values (11, 14)"));
		performQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e12_e13 (e12_id, e13_id) values (12, 16)"));

		// excluding ID - can't render multi-column IDs yet
		Response r1 = target("/e12/12/e1213").queryParam("exclude", "id").queryParam("include", "e12")
				.queryParam("include", "e13").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"e12\":{\"id\":12},\"e13\":{\"id\":16}}],\"total\":1}", r1.readEntity(String.class));
	}
}
