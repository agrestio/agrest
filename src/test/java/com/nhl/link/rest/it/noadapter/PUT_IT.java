package com.nhl.link.rest.it.noadapter;

import static com.nhl.link.rest.unit.matcher.LRMatchers.okAndHasData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.resource.E17Resource;
import com.nhl.link.rest.it.fixture.resource.E2Resource;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E14;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E7;
import com.nhl.link.rest.it.fixture.cayenne.E8;
import com.nhl.link.rest.it.fixture.resource.E14Resource;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import com.nhl.link.rest.it.fixture.resource.E4Resource;
import com.nhl.link.rest.it.fixture.resource.E7Resource;
import com.nhl.link.rest.it.fixture.resource.E8Resource;

public class PUT_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E2Resource.class);
		context.register(E3Resource.class);
		context.register(E4Resource.class);
		context.register(E7Resource.class);
		context.register(E8Resource.class);
		context.register(E14Resource.class);
		context.register(E17Resource.class);
	}

	@Test
	public void test_PUT() throws WebApplicationException, IOException {
		insert("e4", "id, c_varchar", "1, 'xxx'");
		insert("e4", "id, c_varchar", "8, 'yyy'");

		Response response = target("/e4/8").request().put(jsonEntity("{\"id\":8,\"cVarchar\":\"zzz\"}"));

		assertThat(response, okAndHasData(1,
				"[{\"id\":8,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
						+ "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"zzz\"}]"));

		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e4 WHERE id = 8 AND c_varchar = 'zzz'"));
	}

	@Test
	public void test_PUT_ExplicitCompoundId() throws WebApplicationException, IOException {
		insert("e17", "id1, id2, name", "1, 1, 'aaa'");
		insert("e17", "id1, id2, name", "2, 2, 'bbb'");

		Response response = target("/e17").queryParam("id1", 1).queryParam("id2", 1).request()
				.put(jsonEntity("{\"name\":\"xxx\"}"));

		assertThat(response, okAndHasData(1,
				"[{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}]"));

		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e17 WHERE id1 = 1 AND id2 = 1 AND name = 'xxx'"));
	}

	@Test
	public void testPut_ToOne() throws WebApplicationException, IOException {
		insert("e2", "id, name", "1, 'xxx'");
		insert("e2", "id, name", "8, 'yyy'");
		insert("e3", "id, name, e2_id", "3, 'zzz', 8");

		Response response = target("/e3/3").request().put(jsonEntity("{\"id\":3,\"e2\":1}"));
		assertThat(response, okAndHasData(1, "[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}]"));
		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id = 1"));
	}

	@Test
	public void testPut_ToOne_ArraySyntax() throws WebApplicationException, IOException {
		insert("e2", "id, name", "1, 'xxx'");
		insert("e2", "id, name", "8, 'yyy'");
		insert("e3", "id, name, e2_id", "3, 'zzz', 8");

		Response response = target("/e3/3").request().put(jsonEntity("{\"id\":3,\"e2\":[1]}"));
		assertThat(response, okAndHasData(1, "[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}]"));
		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id = 1"));
	}

	@Test
	public void testPut_ToOne_ToNull() throws WebApplicationException, IOException {

		insert("e2", "id, name", "1, 'xxx'");
		insert("e2", "id, name", "8, 'yyy'");
		insert("e3", "id, name, e2_id", "3, 'zzz', 8");

		Response response = target("/e3/3").request().put(jsonEntity("{\"id\":3,\"e2\":null}"));
		assertThat(response, okAndHasData(1, "[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}]"));

		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id IS NULL"));
	}

	@Test
	public void testPut_ToOne_FromNull() throws WebApplicationException, IOException {

		insert("e2", "id, name", "1, 'xxx'");
		insert("e2", "id, name", "8, 'yyy'");
		insert("e3", "id, name, e2_id", "3, 'zzz', null");

		Entity<String> entity = jsonEntity("{\"id\":3,\"e2\":8}");
		Response response = target("/e3/3").request().put(entity);
		assertThat(response, okAndHasData(1, "[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}]"));

		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id  = 8"));
	}

	@Test
	public void testPUT_Bulk() throws WebApplicationException, IOException {

		insert("e3", "id, name", "5, 'aaa'");
		insert("e3", "id, name", "4, 'zzz'");
		insert("e3", "id, name", "2, 'bbb'");
		insert("e3", "id, name", "6, 'yyy'");

		Entity<String> entity = jsonEntity("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]");
		Response response = target("/e3/").queryParam("exclude", "id").queryParam("include", E3.NAME.getName())
				.request().put(entity);

		// ordering must be preserved in response, so comparing with request
		// entity
		assertThat(response,
				okAndHasData(4, "[{\"name\":\"yyy\"},{\"name\":\"zzz\"},{\"name\":\"111\"},{\"name\":\"333\"}]"));
	}

	@Test
	public void testPUT_Single_LongId_Small() throws WebApplicationException, IOException {

		insert("e14", "long_id, name", "5, 'aaa'");

		Entity<String> entity = jsonEntity("[{\"id\":5,\"name\":\"bbb\"}]");
		Response response = target("/e14/5/").queryParam("exclude", "id").queryParam("include", E14.NAME.getName())
				.request().put(entity);

		assertThat(response, okAndHasData(1, entity));

		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e14"));
		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e14 WHERE long_id = 5 AND NAME = 'bbb'"));
	}

	@Test
	public void testPUT_Bulk_LongId_Small() throws WebApplicationException, IOException {

		insert("e14", "long_id, name", "5, 'aaa'");
		insert("e14", "long_id, name", "4, 'zzz'");
		insert("e14", "long_id, name", "2, 'bbb'");
		insert("e14", "long_id, name", "6, 'yyy'");

		Entity<String> entity = jsonEntity("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},"
				+ "{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]");
		Response response = target("/e14/").queryParam("exclude", "id").queryParam("include", E14.NAME.getName())
				.request().put(entity);

		assertThat(response, okAndHasData(4, entity));

		assertEquals(4, intForQuery("SELECT COUNT(1) FROM utest.e14"));
		assertEquals(4, intForQuery("SELECT COUNT(1) FROM utest.e14 WHERE long_id IN (2,4,6,5)"));
	}

	@Test
	public void testPUT_Bulk_LongId() throws WebApplicationException, IOException {

		insert("e14", "long_id, name", "8147483647, 'aaa'");
		insert("e14", "long_id, name", "8147483648, 'zzz'");
		insert("e14", "long_id, name", "8147483649, 'bbb'");
		insert("e14", "long_id, name", "3147483646, 'yyy'");

		Entity<String> putEntity = jsonEntity("[{\"id\":3147483646,\"name\":\"yyy\"},{\"id\":8147483648,\"name\":\"zzz\"}"
				+ ",{\"id\":8147483647,\"name\":\"111\"},{\"id\":8147483649,\"name\":\"333\"}]");
		Response response = target("/e14/").request().put(putEntity);

		assertThat(response, okAndHasData(4, putEntity));

		assertEquals(4, intForQuery("SELECT COUNT(1) FROM utest.e14"));
		assertEquals(4, intForQuery("SELECT count(1) FROM utest.e14 WHERE "
				+ "long_id IN (3147483646, 8147483648, 8147483647, 8147483649)"));
	}

	@Test
	public void testPUT_Bulk_ResponseAttributesFilter() throws WebApplicationException, IOException {

		Entity<String> entity1 = jsonEntity("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"}]");
		Response response1 = target("/e7").queryParam("exclude", "id").queryParam("include", E7.NAME.getName())
				.request().put(entity1);
		assertThat(response1, okAndHasData(2, "[{\"name\":\"yyy\"},{\"name\":\"zzz\"}]"));

		Entity<String> entity2 = jsonEntity("[{\"id\":6,\"name\":\"123\"},{\"id\":4}]");
		Response response2 = target("/e7").queryParam("include", "id").queryParam("exclude", E7.NAME.getName())
				.request().put(entity2);
		assertThat(response2, okAndHasData(2, "[{\"id\":6},{\"id\":4}]"));
	}

	@Test
	public void testPUT_Bulk_ResponseToOneRelationshipFilter() throws WebApplicationException, IOException {

		insert("e8", "id, name", "5, 'aaa'");
		insert("e8", "id, name", "6, 'ert'");

		insert("e9", "e8_id", "5");
		insert("e9", "e8_id", "6");

		Entity<String> entity1 = jsonEntity("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"}]");
		Response response1 = target("/e7").queryParam("include", "id").queryParam("exclude", E7.NAME.getName())
				.queryParam("include", E7.E8.getName()).request().put(entity1);

		assertThat(response1, okAndHasData(2, "[{\"id\":6,\"e8\":null},{\"id\":4,\"e8\":null}]"));

		Entity<String> entity2 = jsonEntity("[{\"id\":6,\"name\":\"123\",\"e8\":6},{\"id\":4,\"name\":\"zzz\",\"e8\":5}]");
		Response response2 = target("/e7").queryParam("include", "id").queryParam("exclude", E7.NAME.getName())
				.queryParam("include", E7.E8.getName()).request().put(entity2);
		assertThat(response2, okAndHasData(2, "[{\"id\":6,\"e8\":{\"id\":6,\"name\":\"ert\"}},"
				+ "{\"id\":4,\"e8\":{\"id\":5,\"name\":\"aaa\"}}]"));

		Entity<String> entity3 = jsonEntity("[{\"id\":6,\"name\":\"123\",\"e8\":6},{\"id\":4,\"name\":\"zzz\",\"e8\":5}]");
		Response response3 = target("/e7").queryParam("include", "id").queryParam("exclude", E7.NAME.getName())
				.queryParam("include", E7.E8.dot(E8.NAME).getName()).request().put(entity3);
		assertThat(response3, okAndHasData(2, "[{\"id\":6,\"e8\":{\"name\":\"ert\"}},"
				+ "{\"id\":4,\"e8\":{\"name\":\"aaa\"}}]"));

		Entity<String> entity4 = jsonEntity("[{\"id\":6,\"name\":\"123\",\"e8\":6},{\"id\":4,\"name\":\"zzz\",\"e8\":5}]");
		Response response4 = target("/e7").queryParam("include", "id").queryParam("exclude", E7.NAME.getName())
				.queryParam("include", E7.E8.dot(E8.E9).getName()).request().put(entity4);
		assertThat(response4, okAndHasData(2, "[{\"id\":6,\"e8\":{\"e9\":{\"id\":6}}},"
				+ "{\"id\":4,\"e8\":{\"e9\":{\"id\":5}}}]"));
	}

	@Test
	public void testPUT_Bulk_ResponseToManyRelationshipFilter() throws WebApplicationException, IOException {

		insert("e8", "id, name", "5, 'aaa'");
		insert("e8", "id, name", "6, 'ert'");

		insert("e7", "id, e8_id, name", "45, 6, 'me'");
		insert("e7", "id, e8_id, name", "78, 5, 'her'");
		insert("e7", "id, e8_id, name", "81, 5, 'him'");

		Entity<String> entity1 = jsonEntity("[{\"id\":6,\"name\":\"yyy\"},{\"id\":5,\"name\":\"zzz\"}]");
		Response response1 = target("/e8").queryParam("include", "id").queryParam("exclude", E8.NAME.getName())
				.queryParam("include", E8.E7S.dot(E7.NAME).getName()).request().put(entity1);

		assertThat(response1, okAndHasData(2, "[{\"id\":6,\"e7s\":[{\"name\":\"me\"}]},"
				+ "{\"id\":5,\"e7s\":[{\"name\":\"her\"},{\"name\":\"him\"}]}]"));
	}

	@Test
	public void testPUT_Single_ResponseToOneRelationshipFilter() throws WebApplicationException, IOException {

		insert("e8", "id, name", "5, 'aaa'");
		insert("e8", "id, name", "6, 'ert'");

		insert("e9", "e8_id", "5");
		insert("e9", "e8_id", "6");

		Entity<String> entity1 = jsonEntity("[{\"name\":\"yyy\",\"e8\":6}]");
		Response response1 = target("/e7/6").queryParam("include", "id").queryParam("exclude", E7.NAME.getName())
				.queryParam("include", E7.E8.dot(E8.E9).getName()).request().put(entity1);

		assertThat(response1, okAndHasData(1, "[{\"id\":6,\"e8\":{\"e9\":{\"id\":6}}}]"));
	}

	@Test
	public void testPut_ToMany() throws WebApplicationException, IOException {

		insert("e2", "id, name", "1, 'xxx'");
		insert("e2", "id, name", "8, 'yyy'");
		insert("e3", "id, name, e2_id", "3, 'zzz', null");
		insert("e3", "id, name, e2_id", "4, 'aaa', 8");
		insert("e3", "id, name, e2_id", "5, 'bbb', 8");

		Response response = target("/e2/1").queryParam("include", E2.E3S.getName())
				.queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(),
						E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
				.request().put(jsonEntity("{\"e3s\":[3,4,5]}"));

		assertThat(response, okAndHasData(1, "[{\"id\":1,\"e3s\":[{\"id\":3},{\"id\":4},{\"id\":5}]}]"));
		assertEquals(3, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE e2_id = 1"));
	}

	@Test
	public void testPut_ToMany_UnrelateAll() throws WebApplicationException, IOException {

		insert("e2", "id, name", "1, 'xxx'");
		insert("e2", "id, name", "8, 'yyy'");
		insert("e3", "id, name, e2_id", "3, 'zzz', null");
		insert("e3", "id, name, e2_id", "4, 'aaa', 8");
		insert("e3", "id, name, e2_id", "5, 'bbb', 8");

		Response response = target("/e2/8").queryParam("include", E2.E3S.getName())
				.queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(),
						E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
				.request().put(jsonEntity("{\"e3s\":[]}"));

		assertThat(response, okAndHasData(1, "[{\"id\":8,\"e3s\":[]}]"));
		assertEquals(3, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE e2_id IS NULL"));
	}

}
