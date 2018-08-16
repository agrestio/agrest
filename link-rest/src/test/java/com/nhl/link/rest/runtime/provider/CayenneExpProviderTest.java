package com.nhl.link.rest.runtime.provider;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.protocol.Exclude;
import com.nhl.link.rest.protocol.Include;
import com.nhl.link.rest.protocol.Sort;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CayenneExpProviderTest extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(Resource.class);
	}

	@Test
	public void test_CayenneExp_Map() {


		Response r1 = target("/e2_Map").queryParam("cayenneExp", urlEnc("{\"exp\":\"name = 'yyy'\"}")).request().get();

		assertNotNull(r1);
	}

	@Test
	public void test_CayenneExp_Map_Params() {


		Response r1 = target("/e2_Map_Params").queryParam("cayenneExp", urlEnc("{\"exp\":\"name = $n\",\"params\":{\"n\":\"xxx\"}}")).request().get();

		assertNotNull(r1);
	}

	@Test
	public void test_CayenneExp_Bare() {


		Response r1 = target("/e2_Bare").queryParam("cayenneExp", urlEnc("name = 'yyy'"))
				.request().get();

		assertNotNull(r1);
	}

	@Test
	public void test_CayenneExp_List() {


		Response r1 = target("/e2_List").queryParam("cayenneExp", urlEnc("[\"name = 'yyy'\"]"))
				.request().get();

		assertNotNull(r1);
	}

	@Test
	public void test_CayenneExp_List_Params() {


		Response r1 = target("/e2_List_Params").queryParam("cayenneExp", urlEnc("[\"name = $b\", \"xxx\"]")).request().get();

		assertNotNull(r1);
	}

	@Test
	public void test_Select_CayenneExp_In_Array() {


		Response r1 = target("/e3_In_Array").queryParam("cayenneExp", urlEnc("{\"exp\":\"e2 in $ids\",\"params\":{\"ids\": [3, 4]}}")).request()
				.get();

		assertNotNull(r1);
	}


	@Path("")
	public static class Resource {

		@Context
		private Configuration config;

		@GET
		@Path("e2_Map")
		public DataResponse<E2> getE2_Map(@QueryParam("cayenneExp") CayenneExp cayenneExp) {

			assertNotNull(cayenneExp);
			assertEquals("name = 'yyy'", cayenneExp.getExp());
			assertTrue(cayenneExp.getParams().isEmpty());
			assertTrue(cayenneExp.getInPositionParams().isEmpty());

			return DataResponse.forType(E2.class);
		}

		@GET
		@Path("e2_Map_Params")
		public DataResponse<E2> getE2_Map_Params(@QueryParam("cayenneExp") CayenneExp cayenneExp) {

			assertNotNull(cayenneExp);
			assertEquals("name = $n", cayenneExp.getExp());
			assertNotNull(cayenneExp.getParams());
			assertEquals(1, cayenneExp.getParams().size());
			assertEquals("\"xxx\"", cayenneExp.getParams().get("n").toString());
			assertTrue(cayenneExp.getInPositionParams().isEmpty());

			return DataResponse.forType(E2.class);
		}

		@GET
		@Path("e2_Bare")
		public DataResponse<E2> getE2_Bare(@QueryParam("cayenneExp") CayenneExp cayenneExp) {

			assertNotNull(cayenneExp);
			assertEquals("name = 'yyy'", cayenneExp.getExp());
			assertTrue(cayenneExp.getParams().isEmpty());
			assertTrue(cayenneExp.getInPositionParams().isEmpty());

			return DataResponse.forType(E2.class);
		}

		@GET
		@Path("e2_List")
		public DataResponse<E2> getE2_List(@QueryParam("cayenneExp") CayenneExp cayenneExp) {

			assertNotNull(cayenneExp);
			assertEquals("name = 'yyy'", cayenneExp.getExp());
			assertTrue(cayenneExp.getParams().isEmpty());
			assertTrue(cayenneExp.getInPositionParams().isEmpty());

			return DataResponse.forType(E2.class);
		}

		@GET
		@Path("e2_List_Params")
		public DataResponse<E2> getE2_List_Params(@QueryParam("cayenneExp") CayenneExp cayenneExp) {

			assertNotNull(cayenneExp);
			assertEquals("name = $b", cayenneExp.getExp());
			assertNotNull(cayenneExp.getParams());
			assertTrue(cayenneExp.getParams().isEmpty());
			assertEquals(1, cayenneExp.getInPositionParams().size());
			assertEquals("\"xxx\"", cayenneExp.getInPositionParams().get(0).toString());

			return DataResponse.forType(E2.class);
		}


		@GET
		@Path("e3_In_Array")
		public DataResponse<E3> getE3_In_Array(@QueryParam("sort") Sort sort,
									  @QueryParam("include") List<Include> includes,
									  @QueryParam("exclude") List<Exclude> excludes,
									  @QueryParam("cayenneExp") CayenneExp cayenneExp) {

			assertNotNull(cayenneExp);
			assertEquals("e2 in $ids", cayenneExp.getExp());
			assertNotNull(cayenneExp.getParams());
			assertEquals(1, cayenneExp.getParams().size());
			assertNotNull(cayenneExp.getParams().get("ids"));
			assertEquals("[3, 4]", cayenneExp.getParams().get("ids").toString());
			assertTrue(cayenneExp.getInPositionParams().isEmpty());

			return DataResponse.forType(E3.class);
		}
	}
}
