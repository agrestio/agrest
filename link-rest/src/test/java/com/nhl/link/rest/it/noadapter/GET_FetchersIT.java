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
import com.nhl.link.rest.it.fixture.resource.E22Resource;

public class GET_FetchersIT extends JerseyTestOnDerby {

	@Before
	public void loadData() {
		newContext()
				.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e22 (id, name) values (1, 'xxx')"));
		newContext()
				.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e22 (id, name) values (2, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e23 (id, e22_id, name) values (8, 1, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e23 (id, e22_id, name) values (9, 1, 'zzz')"));
	}

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E22Resource.class);
	}
	
	@Test
	public void testMultiFetcher_ParentAware() {
		Response response1 = target("/e22/parent-aware-strategy").queryParam("include", "id")
				.queryParam("include", "pojo").queryParam("include", "e23s.id").queryParam("sort", "id").request()
				.get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());

		assertEquals(
				"{\"data\":[{\"id\":1,\"e23s\":[{\"id\":8},{\"id\":9}],\"pojo\":{\"integer\":1,\"string\":\"s_1\"}},"
						+ "{\"id\":2,\"e23s\":[],\"pojo\":{\"integer\":2,\"string\":\"s_2\"}}],\"total\":2}",
				response1.readEntity(String.class));
	}

	@Test
	public void testMultiFetcher_ParentAgnostic() {
		Response response1 = target("/e22/parent-agnostic-strategy").queryParam("include", "id")
				.queryParam("include", "pojo").queryParam("include", "e23s.id").queryParam("sort", "id").request()
				.get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());

		assertEquals(
				"{\"data\":[{\"id\":1,\"e23s\":[{\"id\":8},{\"id\":9}],\"pojo\":{\"integer\":1,\"string\":\"s_1\"}},"
						+ "{\"id\":2,\"e23s\":[],\"pojo\":{\"integer\":2,\"string\":\"s_2\"}}],\"total\":2}",
				response1.readEntity(String.class));
	}

	@Test
	public void testMultiFetcher_ParentAgnostic_Fetcher_Error() {
		Response response1 = target("/e22/parent-agnostic-strategy-fetcher-error").queryParam("include", "id")
				.queryParam("include", "pojo").queryParam("include", "e23s.id").queryParam("sort", "id").request()
				.get();

		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getStatus());
	}

	@Test
	public void testMultiFetcher_PerParent() {
		Response response1 = target("/e22/per-parent-strategy").queryParam("include", "id")
				.queryParam("include", "pojo").queryParam("include", "e23s.id").queryParam("sort", "id").request()
				.get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());

		assertEquals(
				"{\"data\":[{\"id\":1,\"e23s\":[{\"id\":8},{\"id\":9}],\"pojo\":{\"integer\":1,\"string\":\"s_1\"}},"
						+ "{\"id\":2,\"e23s\":[],\"pojo\":{\"integer\":2,\"string\":\"s_2\"}}],\"total\":2}",
				response1.readEntity(String.class));
	}
	
	@Test
	public void testMultiFetcher_PerParent_Fetcher_Error() {
		Response response1 = target("/e22/per-parent-strategy-fetcher-error").queryParam("include", "id")
				.queryParam("include", "pojo").queryParam("include", "e23s.id").queryParam("sort", "id").request()
				.get();

		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response1.getStatus());
	}

}
