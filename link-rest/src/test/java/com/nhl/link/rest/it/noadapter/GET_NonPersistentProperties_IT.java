package com.nhl.link.rest.it.noadapter;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.resource.E14Resource;
import com.nhl.link.rest.it.fixture.resource.E15Resource;

public class GET_NonPersistentProperties_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E14Resource.class);
		context.register(E15Resource.class);
	}

	@Test
	public void testGET_Root() {
		insert("e15", "long_id, name", "1, 'xxx'");
		insert("e14", "e15_id, long_id, name", "1, 8, 'yyy'");

		Response response1 = target("/e14").queryParam("include", "name").queryParam("include", "prettyName").request()
				.get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void testGET_Root_SortByNonPersistentProperty() {

		insert("e14", "long_id, name", "7, 'xxx'");
		insert("e14", "long_id, name", "8, 'yyy'");
		insert("e14", "long_id, name", "9, 'zzz'");

		Response response1 = target("/e14").queryParam("include", "name").queryParam("include", "prettyName")
				.queryParam("sort", urlEnc("[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"prettyName\"}]"))
				.request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"name\":\"zzz\",\"prettyName\":\"zzz_pretty\"}," +
				"{\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}," +
				"{\"name\":\"xxx\",\"prettyName\":\"xxx_pretty\"}],\"total\":3}",
				response1.readEntity(String.class));
	}

	@Test
	public void testGET_Root_SortByRelatedObjectsNonPersistentProperty() {

		insert("e15", "long_id, name", "1, 'aaa'");
		insert("e15", "long_id, name", "2, 'bbb'");
		insert("e15", "long_id, name", "3, 'ccc'");

		insert("e14", "e15_id, long_id, name", "1, 7, 'xxx'");
		insert("e14", "e15_id, long_id, name", "2, 8, 'yyy'");
		insert("e14", "e15_id, long_id, name", "3, 9, 'zzz'");

		Response response1 = target("/e14").queryParam("include", "name")
				.queryParam("sort", urlEnc("[{\"property\":\"name\"},{\"property\":\"e15.nonPersistent\"}]"))
				.request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"name\":\"xxx\"},{\"name\":\"yyy\"},{\"name\":\"zzz\"}],\"total\":3}",
				response1.readEntity(String.class));
	}

	@Test
	public void testGET_PrefetchPojoRel() {
		insert("e15", "long_id, name", "1, 'xxx'");
		insert("e14", "e15_id, long_id, name", "1, 8, 'yyy'");

		Response response1 = target("/e14").queryParam("include", "name").queryParam("include", "p7").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());

		assertEquals("{\"data\":[{\"name\":\"yyy\",\"p7\":{\"id\":800,\"string\":\"p7_yyy\"}}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void testGET_Related() {
		insert("e15", "long_id, name", "1, 'xxx'");
		insert("e14", "e15_id, long_id, name", "1, 8, 'yyy'");

		Response response1 = target("/e15").queryParam("include", "e14s.name").queryParam("include", "e14s.prettyName")
				.queryParam("exclude", "nonPersistent").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals(
				"{\"data\":"
						+ "[{\"id\":1,\"e14s\":[{\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}],\"name\":\"xxx\"}],\"total\":1}",
				response1.readEntity(String.class));
	}
}
