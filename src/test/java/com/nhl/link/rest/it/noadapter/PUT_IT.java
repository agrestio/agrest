package com.nhl.link.rest.it.noadapter;

import static com.nhl.link.rest.unit.matcher.LRMatchers.okAndHasData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.resource.E14Resource;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import com.nhl.link.rest.it.fixture.resource.E4Resource;

public class PUT_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E3Resource.class);
		context.register(E4Resource.class);
		context.register(E14Resource.class);
	}

	@Test
	public void testPut() throws WebApplicationException, IOException {
		insert("e4", "id, c_varchar", "1, 'xxx'");
		insert("e4", "id, c_varchar", "8, 'yyy'");

		Response response = target("/e4/8").request().put(jsonEntity("{\"id\":8,\"cVarchar\":\"zzz\"}"));

		assertThat(response, okAndHasData(1,
				"[{\"id\":8,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
						+ "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"zzz\"}]"));

		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e4 WHERE id = 8 AND c_varchar = 'zzz'"));
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

		Entity<String> putEntity = jsonEntity("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]");
		Response response = target("/e3/").queryParam("exclude", "id").queryParam("include", E3.NAME.getName())
				.request().put(putEntity);

		// ordering must be preserved in response, so comparing with request
		// entity
		assertThat(response,
				okAndHasData(4, "[{\"name\":\"yyy\"},{\"name\":\"zzz\"},{\"name\":\"111\"},{\"name\":\"333\"}]"));
	}

	@Test
	public void testPUT_Bulk_LongId_Small() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (5, 'aaa')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (4, 'zzz')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (2, 'bbb')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (6, 'yyy')"));

		Entity<String> entity = jsonEntity("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},"
				+ "{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]");
		Response response = target("/e14/").queryParam("exclude", "id").queryParam("include", E3.NAME.getName())
				.request().put(entity);

		// ordering must be preserved in response, so comparing with request
		// entity
		assertThat(response, okAndHasData(4, entity));

		assertEquals(4, intForQuery("SELECT COUNT(1) FROM utest.e14"));
		assertEquals(4, intForQuery("SELECT COUNT(1) FROM utest.e14 WHERE long_id IN (2,4,6,5)"));
	}

	@Test
	public void testPUT_Bulk_LongId() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (8147483647, 'aaa')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (8147483648, 'zzz')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (8147483649, 'bbb')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (3147483646, 'yyy')"));

		Entity<String> putEntity = jsonEntity("[{\"id\":3147483646,\"name\":\"yyy\"},{\"id\":8147483648,\"name\":\"zzz\"}"
				+ ",{\"id\":8147483647,\"name\":\"111\"},{\"id\":8147483649,\"name\":\"333\"}]");
		Response response = target("/e14/").request().put(putEntity);

		// ordering must be preserved in response, so comparing with request
		// entity
		assertThat(response, okAndHasData(4, putEntity));

		assertEquals(4, intForQuery("SELECT COUNT(1) FROM utest.e14"));
		assertEquals(4, intForQuery("SELECT count(1) FROM utest.e14 WHERE "
				+ "long_id IN (3147483646, 8147483648, 8147483647, 8147483649)"));
	}
}
