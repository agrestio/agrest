package com.nhl.link.rest.it;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E10;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.resource.E10Resource;
import com.nhl.link.rest.it.fixture.resource.E4Resource;

public class GET_ConstraintsIT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E4Resource.class);
		context.register(E10Resource.class);
	}

	@Test
	public void test_Implicit() throws WebApplicationException, IOException {

		SQLTemplate insert = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
		newContext().performGenericQuery(insert);

		Response response1 = target("/e4/limit_attributes").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":1,\"cInt\":5}],\"total\":1}", response1.readEntity(String.class));

	}

	@Test
	public void test_Explicit() throws WebApplicationException, IOException {

		SQLTemplate insert = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
		newContext().performGenericQuery(insert);

		Response response1 = target("/e4/limit_attributes").queryParam("include", E4.C_BOOLEAN.getName())
				.queryParam("include", E4.C_INT.getName()).request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"cInt\":5}],\"total\":1}", response1.readEntity(String.class));

	}

	@Test
	public void test_Annotated() throws WebApplicationException, IOException {

		insert("e10", "id, c_varchar, c_int, c_boolean, c_date", "1, 'xxx', 5, true, '2014-01-02'");

		Response response1 = target("/e10").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":1,\"cBoolean\":true,\"cInt\":5}],\"total\":1}",
				response1.readEntity(String.class));

	}

	@Test
	public void test_Annotated_Relationship() throws WebApplicationException, IOException {

		insert("e10", "id, c_varchar, c_int, c_boolean, c_date", "1, 'xxx', 5, true, '2014-01-02'");
		insert("e11", "id, e10_id, address, name", "15, 1, 'aaa', 'nnn'");

		Response response1 = target("/e10").queryParam("include", E10.E11S.getName()).request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":1,\"cBoolean\":true,\"cInt\":5,\"e11s\":{\"address\":\"aaa\"}}],\"total\":1}",
				response1.readEntity(String.class));

	}
}
