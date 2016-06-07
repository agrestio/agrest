package com.nhl.link.rest.it.noadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Time;
import java.util.Collections;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.it.fixture.cayenne.E17;
import com.nhl.link.rest.it.fixture.cayenne.E19;
import com.nhl.link.rest.it.fixture.resource.E17Resource;
import com.nhl.link.rest.it.fixture.resource.E19Resource;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.SQLTemplate;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.resource.E2Resource;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import com.nhl.link.rest.it.fixture.resource.E4Resource;
import com.nhl.link.rest.it.fixture.resource.E6Resource;

public class GET_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E2Resource.class);
		context.register(E3Resource.class);
		context.register(E4Resource.class);
		context.register(E6Resource.class);
		context.register(E17Resource.class);
		context.register(E19Resource.class);
	}

	@Test
	public void testResponse() throws WebApplicationException, IOException {

		SQLTemplate insert = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
		newContext().performGenericQuery(insert);

		Response response1 = target("/e4").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":" + "[{\"id\":1,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
				+ "\"cInt\":5,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"xxx\"}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void testDateTime() throws WebApplicationException, IOException {

		DateTime ts = new DateTime("2012-02-03T11:01:02Z");

		SQLTemplate insert = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (c_timestamp) values (#bind($ts 'TIMESTAMP'))");
		insert.setParams(Collections.singletonMap("ts", ts.toDate()));
		newContext().performGenericQuery(insert);

		Response response1 = target("/e4").queryParam("include", E4.C_TIMESTAMP.getName()).request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"cTimestamp\":\"2012-02-03T11:01:02Z\"}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void testDate() throws WebApplicationException, IOException {

		DateTime date = new DateTime("2012-02-03");

		SQLTemplate insert = new SQLTemplate(E4.class, "INSERT INTO utest.e4 (c_date) values (#bind($date 'DATE'))");
		insert.setParams(Collections.singletonMap("date", date.toDate()));
		newContext().performGenericQuery(insert);

		Response response1 = target("/e4").queryParam("include", E4.C_DATE.getName()).request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"cDate\":\"2012-02-03\"}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void testTime() throws WebApplicationException, IOException {

		LocalTime lt = new LocalTime(14, 0, 1);

		// "14:00:01"
		Time time = new Time(lt.toDateTimeToday().getMillis());

		SQLTemplate insert = new SQLTemplate(E4.class, "INSERT INTO utest.e4 (c_time) values (#bind($time 'TIME'))");
		insert.setParams(Collections.singletonMap("time", time));
		newContext().performGenericQuery(insert);

		Response response1 = target("/e4").queryParam("include", E4.C_TIME.getName()).request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"cTime\":\"14:00:01\"}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void test_Sort_ById() throws WebApplicationException, IOException {

		newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id) values (2)"));
		newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id) values (1)"));
		newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id) values (3)"));

		Response response1 = target("/e4").queryParam("sort", urlEnc("[{\"property\":\"id\",\"direction\":\"DESC\"}]"))
				.queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":3},{\"id\":2},{\"id\":1}],\"total\":3}",
				response1.readEntity(String.class));
	}

	@Test
	public void test_Sort_Invalid() throws WebApplicationException, IOException {

		Response response1 = target("/e4")
				.queryParam("sort", urlEnc("[{\"property\":\"xyz\",\"direction\":\"DESC\"}]"))
				.queryParam("include", "id").request().get();

		assertEquals(Status.BAD_REQUEST.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"Invalid path 'xyz' for 'E4'\"}",
				response1.readEntity(String.class));
	}

	@Test
	// this is a hack for Sencha bug, passing us null sorters per LF-189...
	// allowing for lax property name checking as a result
	public void test_Sort_Null() throws WebApplicationException, IOException {

		newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id) values (2)"));
		newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id) values (1)"));
		newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id) values (3)"));

		Response response1 = target("/e4").queryParam("sort", urlEnc("[{\"property\":null,\"direction\":\"DESC\"}]"))
				.queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertTrue(response1.readEntity(String.class).endsWith("total\":3}"));
	}

	@Test
	public void test_SelectById() throws WebApplicationException, IOException {

		newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id) values (2)"));

		Response response1 = target("/e4/2").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":2,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
				+ "\"cInt\":null,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":null}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void test_SelectById_Params() throws WebApplicationException, IOException {

		newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id) values (2)"));

		Response response1 = target("/e4/ie/2").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":2,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
				+ "\"cInt\":null,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":null}],\"total\":1}",
				response1.readEntity(String.class));

		Response response2 = target("/e4/ie/2").queryParam("include", "id").request().get();
		assertEquals(Status.OK.getStatusCode(), response2.getStatus());
		assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", response2.readEntity(String.class));
	}

	@Test
	public void test_SelectById_NotFound() throws WebApplicationException, IOException {

		Response response1 = target("/e4/2").request().get();

		assertEquals(Status.NOT_FOUND.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"No object for ID '2' and entity 'E4'\"}",
				response1.readEntity(String.class));
	}

	@Test
	public void test_SelectById_Prefetching() throws WebApplicationException, IOException {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response response1 = target("/e3/8").queryParam("include", "e2.id").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals(
				"{\"data\":[{\"id\":8,\"e2\":{\"id\":1},\"name\":\"yyy\",\"phoneNumber\":null}],\"total\":1}",
				response1.readEntity(String.class));

		Response response2 = target("/e3/8").queryParam("include", "e2.name").request().get();

		assertEquals(Status.OK.getStatusCode(), response2.getStatus());
		assertEquals(
				"{\"data\":[{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\",\"phoneNumber\":null}],\"total\":1}",
				response2.readEntity(String.class));

		Response response3 = target("/e2/1").queryParam("include", "e3s.id").request().get();

		assertEquals(Status.OK.getStatusCode(), response3.getStatus());
		assertEquals("{\"data\":[{\"id\":1,\"address\":null,\"e3s\":"
				+ "[{\"id\":8},{\"id\":9}],\"name\":\"xxx\"}],\"total\":1}", response3.readEntity(String.class));
	}

	@Test
	public void test_Select_Prefetching() throws WebApplicationException, IOException {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response response1 = target("/e3").queryParam("include", "id").queryParam("include", "e2.id")
				.queryParam("sort", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":8,\"e2\":{\"id\":1}},"
				+ "{\"id\":9,\"e2\":{\"id\":1}}],\"total\":2}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_RelationshipSort() throws WebApplicationException, IOException {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'zzz')"));
		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (3, 'xxx')"));

		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'aaa')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 2, 'bbb')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (10, 3, 'ccc')"));

		Response response1 = target("/e3").queryParam("include", "id").queryParam("include", E3.E2.getName())
				.queryParam("sort", E3.E2.dot(E2.NAME).getName()).request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":10,\"e2\":{\"id\":3,\"address\":null,\"name\":\"xxx\"}}," +
				"{\"id\":9,\"e2\":{\"id\":2,\"address\":null,\"name\":\"yyy\"}}," +
				"{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"zzz\"}}],\"total\":3}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_RelationshipStartLimit() throws WebApplicationException, IOException {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'zzz')"));
		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));

		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'aaa')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'bbb')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (10, 2, 'ccc')"));

		Response response1 = target("/e2")
				.queryParam("include", "id")
				.queryParam("include", URLEncoder.encode("{\"path\":\"" + E2.E3S.getName() + "\",\"start\":1,\"limit\":1}", "UTF-8"))
				.queryParam("exclude", E2.E3S.dot(E3.PHONE_NUMBER).getName())
				.request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":1,\"e3s\":[{\"id\":9,\"name\":\"bbb\"}]}," +
				"{\"id\":2,\"e3s\":[]}],\"total\":2}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_Prefetching_StartLimit() throws WebApplicationException, IOException {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (10, 1, 'zzz')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (11, 1, 'zzz')"));

		Response response1 = target("/e3").queryParam("include", "id").queryParam("include", "e2.id")
				.queryParam("sort", "id").queryParam("start", "1").queryParam("limit", "2").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":9,\"e2\":{\"id\":1}},"
				+ "{\"id\":10,\"e2\":{\"id\":1}}],\"total\":4}", response1.readEntity(String.class));
	}

	@Test
	public void test_SelectToOne_Null() throws WebApplicationException, IOException {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, null, 'zzz')"));

		Response response1 = target("/e3").queryParam("include", "e2.id").queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":8,\"e2\":{\"id\":1}},{\"id\":9,\"e2\":null}],\"total\":2}",
				response1.readEntity(String.class));
	}

	@Test
	public void test_SelectCharPK() throws WebApplicationException, IOException {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e6 (char_id, char_column) values ('a', 'aaa')"));

		Response response1 = target("/e6/a").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":\"a\",\"charColumn\":\"aaa\"}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void test_SelectByCompoundId() {
		newContext().performGenericQuery(
				new SQLTemplate(E17.class, "INSERT INTO utest.e17 (id1, id2, name) values (1, 1, 'aaa')"));

		Response response1 = target("/e17").queryParam("id1", 1).queryParam("id2", 1).request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"aaa\"}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void test_Select_MapByRootEntity() {

		insert("e4", "c_varchar, c_int", "'xxx', 1");
		insert("e4", "c_varchar, c_int", "'yyy', 2");
		insert("e4", "c_varchar, c_int", "'zzz', 2");

		Response response1 = target("/e4").queryParam("mapBy", E4.C_INT.getName())
				.queryParam("include", E4.C_VARCHAR.getName()).request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":{\"1\":[{\"cVarchar\":\"xxx\"}]," +
				"\"2\":[{\"cVarchar\":\"yyy\"},{\"cVarchar\":\"zzz\"}]},\"total\":3}",
				response1.readEntity(String.class));
	}

	@Test
	public void test_SelectById_EscapeLineSeparators() throws WebApplicationException, IOException {

		String s = "First line\u2028Second line...\u2029";
		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e4 (id, c_varchar) values (1, '" + s + "')"));

		Response response1 = target("/e4/1").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":1,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null," +
						"\"cInt\":null,\"cTime\":null,\"cTimestamp\":null," +
						"\"cVarchar\":\"First line\\u2028Second line...\\u2029\"}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void test_SelectByteArrayProperty() throws WebApplicationException, IOException {

		ObjectContext ctx = newContext();
		E19 e19 = ctx.newObject(E19.class);
		e19.setGuid("someValue123".getBytes("UTF-8"));
		ctx.commitChanges();

		Response response1 = target("/e19/" + Cayenne.intPKForObject(e19))
				.queryParam("include", E19.GUID.getName()).request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"guid\":\"c29tZVZhbHVlMTIz\"}],\"total\":1}",
				response1.readEntity(String.class));
	}

}
