package com.nhl.link.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import com.nhl.link.rest.unit.JerseyTestOnDerby;
import com.nhl.link.rest.unit.cayenne.E3;
import com.nhl.link.rest.unit.cayenne.E4;

public class LinkRestService_InContainer_GET_EncoderFilters_Test extends JerseyTestOnDerby {

	@Before
	public void before() {

		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E4"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E3"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E2"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E5"));
	}

	@Override
	protected LinkRestBuilder doConfigure() {
		return super.doConfigure().encoderFilter(new E4OddFilter());
	}

	@Test
	public void testFilteredTotal() {

		runtime.newContext()
				.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) values (1), (2)"));

		Response response1 = target("/lr/e4").queryParam("include", "id").queryParam("sort", "id").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":2}],\"total\":1}", response1.readEntity(String.class));
	}

	@Test
	public void testFilteredPagination1() {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) "
						+ "values (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)"));

		Response response1 = target("/lr/e4").queryParam("include", "id").queryParam("sort", "id")
				.queryParam("start", "0").queryParam("limit", "2").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":2},{\"id\":4}],\"total\":5}",
				response1.readEntity(String.class));
	}

	@Test
	public void testFilteredPagination2() {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) "
						+ "values (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)"));

		Response response1 = target("/lr/e4").queryParam("include", "id").queryParam("sort", "id")
				.queryParam("start", "2").queryParam("limit", "3").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":6},{\"id\":8},{\"id\":10}],\"total\":5}",
				response1.readEntity(String.class));
	}

	@Test
	public void testFilteredPagination3() {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) "
						+ "values (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)"));

		Response response1 = target("/lr/e4").queryParam("include", "id").queryParam("sort", "id")
				.queryParam("start", "2").queryParam("limit", "10").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":6},{\"id\":8},{\"id\":10}],\"total\":5}",
				response1.readEntity(String.class));
	}

	private final class E4OddFilter implements EncoderFilter {
		@Override
		public boolean matches(Entity<?> entity) {
			return entity.getEntity().getName().equals("E4");
		}

		@Override
		public boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate)
				throws IOException {

			E4 e4 = (E4) object;

			// keep even, remove odd
			if (Cayenne.intPKForObject(e4) % 2 == 0) {
				return delegate.encode(propertyName, object, out);
			}

			return false;
		}

		@Override
		public boolean willEncode(String propertyName, Object object, Encoder delegate) {
			E4 e4 = (E4) object;

			// keep even, remove odd
			if (Cayenne.intPKForObject(e4) % 2 == 0) {
				return delegate.willEncode(propertyName, object);
			}

			return false;
		}
	}

}
