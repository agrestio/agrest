package com.nhl.link.rest.incontainer.sencha;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Test;

import com.nhl.link.rest.runtime.LinkRestBuilder;
import com.nhl.link.rest.runtime.adapter.sencha.SenchaAdapter;
import com.nhl.link.rest.unit.JerseyTestOnDerby;
import com.nhl.link.rest.unit.cayenne.E4;
import com.nhl.link.rest.unit.resource.E4Resource;

public class Sencha_POST_Test extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
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

}
