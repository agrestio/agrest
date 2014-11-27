package com.nhl.link.rest.it.sencha;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Test;

import com.nhl.link.rest.it.resource.E3Resource;
import com.nhl.link.rest.it.resource.E4Resource;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import com.nhl.link.rest.runtime.adapter.sencha.SenchaAdapter;
import com.nhl.link.rest.unit.JerseyTestOnDerby;
import com.nhl.link.rest.unit.cayenne.E3;
import com.nhl.link.rest.unit.cayenne.E4;

public class Sencha_POST_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E3Resource.class);
		context.register(E4Resource.class);
	}

	@Override
	protected LinkRestBuilder doConfigure() {
		return super.doConfigure().adapter(new SenchaAdapter());
	}

	@Test
	public void testPost_Default_Data() throws WebApplicationException, IOException {

		Response response1 = target("/e4/defaultdata").request().post(
				Entity.entity("{\"cVarchar\":\"zzz\"}", MediaType.APPLICATION_JSON));
		assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());

		E4 e41 = (E4) Cayenne.objectForQuery(context, new SelectQuery<E4>(E4.class));
		assertEquals("zzz", e41.getCVarchar());
		int id1 = Cayenne.intPKForObject(e41);

		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id1
				+ ",\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
				+ "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"zzz\"}],\"total\":1}",
				response1.readEntity(String.class));
	}
	
	@Test
	public void testPost_ToOne() throws WebApplicationException, IOException {

		context.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));

		Response response1 = target("/e3").request().post(
				Entity.entity("{\"e2_id\":8,\"name\":\"MM\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());

		E3 e3 = (E3) Cayenne.objectForQuery(context, new SelectQuery<E3>(E3.class));
		int id = Cayenne.intPKForObject(e3);

		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id
				+ ",\"name\":\"MM\",\"phoneNumber\":null}],\"total\":1}", response1.readEntity(String.class));

		runtime.newContext().invalidateObjects(e3);
		assertEquals("MM", e3.getName());
		assertEquals(8, Cayenne.intPKForObject(e3.getE2()));
	}
	
	@Test
	public void testPost_ToOne_BadFK() throws WebApplicationException, IOException {

		context.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));

		Response response1 = target("/e3").request().post(
				Entity.entity("{\"e2_id\":15,\"name\":\"MM\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.NOT_FOUND.getStatusCode(), response1.getStatus());

		assertEquals(0, context.select(new SelectQuery<E3>(E3.class)).size());
	}

}
