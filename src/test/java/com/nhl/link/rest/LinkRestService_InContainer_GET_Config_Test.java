package com.nhl.link.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.unit.JerseyTestOnDerby;
import com.nhl.link.rest.unit.cayenne.E4;

public class LinkRestService_InContainer_GET_Config_Test extends JerseyTestOnDerby {

	@Before
	public void before() {
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E4"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E3"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E2"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E5"));
	}

	@Test
	public void test_PathAttribute() throws WebApplicationException, IOException {

		SQLTemplate insert = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
		runtime.newContext().performGenericQuery(insert);

		Response response1 = target("/lrc/limit_attributes").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":1,\"cInt\":5}],\"total\":1}",
				response1.readEntity(String.class));

	}

}
