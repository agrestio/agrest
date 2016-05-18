package com.nhl.link.rest.it.noadapter;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.SQLTemplate;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.resource.E20Resource;

public class GET_FetchersIT extends JerseyTestOnDerby {

	@Before
	public void loadData() {
		newContext()
				.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e20 (id, name) values (1, 'xxx')"));
		newContext()
				.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e20 (id, name) values (2, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e21 (id, e20_id, name) values (8, 1, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e21 (id, e20_id, name) values (9, 1, 'zzz')"));
	}

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E20Resource.class);
	}

	@Test
	public void testMultiFetcher_ParentAgnostic() {
		Response response1 = target("/e20/parent-agnostic-strategy").queryParam("include", "id")
				.queryParam("include", "pojo").queryParam("include", "e21s.id").queryParam("sort", "id").request()
				.get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());

		assertEquals(
				"{\"data\":[{\"id\":1,\"e21s\":[{\"id\":8},{\"id\":9}],\"pojo\":{\"integer\":1,\"string\":\"s_1\"}},"
						+ "{\"id\":2,\"e21s\":[],\"pojo\":{\"integer\":2,\"string\":\"s_2\"}}],\"total\":2}",
				response1.readEntity(String.class));
	}
}
