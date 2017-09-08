package com.nhl.link.rest.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.SQLTemplate;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.listener.FetchCallbackListener;
import com.nhl.link.rest.it.fixture.listener.FetchPassThroughListener;
import com.nhl.link.rest.it.fixture.listener.FetchTakeOverListener;
import com.nhl.link.rest.it.fixture.resource.E3Resource;

public class GET_ListenersIT extends JerseyTestOnDerby {

	@Before
	public void loadData() {
		newContext()
				.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));
	}

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E3Resource.class);
	}

	@Test
	public void testCallbackListener() throws WebApplicationException, IOException {

		FetchCallbackListener.BEFORE_FETCH_CALLED = false;

		Response response1 = target("/e3/callbacklistener").queryParam("include", "id").queryParam("sort", "id")
				.request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertTrue(FetchCallbackListener.BEFORE_FETCH_CALLED);

		assertEquals("{\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}",
				response1.readEntity(String.class));
	}

	@Test
	public void testPassThroughLisetner() throws WebApplicationException, IOException {

		FetchPassThroughListener.BEFORE_FETCH_CALLED = false;

		Response response1 = target("/e3/passthroughlistener").queryParam("include", "id").queryParam("sort", "id")
				.request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertTrue(FetchPassThroughListener.BEFORE_FETCH_CALLED);

		assertEquals("{\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}",
				response1.readEntity(String.class));
	}

	@Test
	public void testTakeOverListener() throws WebApplicationException, IOException {

		FetchTakeOverListener.BEFORE_FETCH_CALLED = false;

		Response response1 = target("/e3/takeoverlistener").queryParam("include", "name").queryParam("sort", "id")
				.request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertTrue(FetchTakeOverListener.BEFORE_FETCH_CALLED);

		assertEquals("{\"data\":[{\"name\":\"__X__\"},{\"name\":\"__Y__\"}],\"total\":2}",
				response1.readEntity(String.class));

	}

}
