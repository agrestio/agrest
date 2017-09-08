package com.nhl.link.rest.it;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.listener.CayennePaginationListener;
import com.nhl.link.rest.it.fixture.resource.E4Resource;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GET_EncoderFilters_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E4Resource.class);
	}

	@Override
	protected LinkRestBuilder doConfigure() {
		return super.doConfigure().encoderFilter(new E4OddFilter());
	}

	@Test
	public void testFilteredTotal() {

		newContext()
				.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) values (1), (2)"));

		Response response1 = target("/e4").queryParam("include", "id").queryParam("sort", "id").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", response1.readEntity(String.class));
	}

	@Test
	public void testFilteredPagination1() {

		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) "
						+ "values (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)"));

		Response response1 = target("/e4").queryParam("include", "id").queryParam("sort", "id")
				.queryParam("start", "0").queryParam("limit", "2").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":2},{\"id\":4}],\"total\":5}",
				response1.readEntity(String.class));
	}

	@Test
	public void testFilteredPagination2() {

		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) "
						+ "values (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)"));

		Response response1 = target("/e4").queryParam("include", "id").queryParam("sort", "id")
				.queryParam("start", "2").queryParam("limit", "3").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":6},{\"id\":8},{\"id\":10}],\"total\":5}",
				response1.readEntity(String.class));
	}

	@Test
	public void testFilteredPagination3() {

		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) "
						+ "values (1), (2), (3), (4), (5), (6), (7), (8), (9), (10)"));

		Response response1 = target("/e4").queryParam("include", "id").queryParam("sort", "id")
				.queryParam("start", "2").queryParam("limit", "10").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":6},{\"id\":8},{\"id\":10}],\"total\":5}",
				response1.readEntity(String.class));
	}

	@Test
	public void testFilteredPagination4_Listeners() {

		CayennePaginationListener.RESOURCE_ENTITY_IS_FILTERED = false;
		CayennePaginationListener.QUERY_PAGE_SIZE = 0;

		target("/e4/pagination_listener").queryParam("include", "id").queryParam("sort", "id")
				.queryParam("start", "2").queryParam("limit", "10").request().get();

		assertTrue(CayennePaginationListener.RESOURCE_ENTITY_IS_FILTERED);
		assertEquals(0, CayennePaginationListener.QUERY_PAGE_SIZE);
	}

	@Test
	public void testFilteredPagination4_CustomStage() {

		CayennePaginationListener.RESOURCE_ENTITY_IS_FILTERED = false;
		CayennePaginationListener.QUERY_PAGE_SIZE = 0;

		target("/e4/pagination_stage").queryParam("include", "id").queryParam("sort", "id")
				.queryParam("start", "2").queryParam("limit", "10").request().get();

		assertTrue(CayennePaginationListener.RESOURCE_ENTITY_IS_FILTERED);
		assertEquals(0, CayennePaginationListener.QUERY_PAGE_SIZE);
	}

	private final class E4OddFilter implements EncoderFilter {
		@Override
		public boolean matches(ResourceEntity<?> entity) {
			return entity.getLrEntity().getName().equals("E4");
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
