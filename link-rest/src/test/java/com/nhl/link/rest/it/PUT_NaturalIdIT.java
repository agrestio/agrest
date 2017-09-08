package com.nhl.link.rest.it;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.resource.E20Resource;
import com.nhl.link.rest.it.fixture.resource.E21Resource;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static com.nhl.link.rest.unit.matcher.LRMatchers.okAndHasData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class PUT_NaturalIdIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E20Resource.class);
		context.register(E21Resource.class);
    }

    @Test
	public void test_PUT() throws WebApplicationException, IOException {
		insert("e20", "name", "'John'");
		insert("e20", "name", "'Brian'");

		Response response = target("/e20/John").request().put(jsonEntity("{\"age\":28,\"description\":\"zzz\"}"));

		assertThat(response,
				okAndHasData(1, "[{\"id\":\"John\",\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}]"));

		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e20 WHERE age = 28 AND description = 'zzz'"));
	}

    @Test
	public void test_PUT_SeveralExistingObjects() throws WebApplicationException, IOException {
		insert("e20", "name", "'John'");
		insert("e20", "name", "'John'");

		Response response = target("/e20/John").request().put(jsonEntity("{\"age\":28,\"description\":\"zzz\"}"));

		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID 'John' and entity 'E20'\"}",
                response.readEntity(String.class));
	}

	@Test
	public void test_PUT_MultiId() throws WebApplicationException, IOException {
		insert("e21", "age, name", "18, 'John'");
		insert("e21", "age, name", "27, 'Brian'");

		Response response = target("/e21/byid").queryParam("age", 18).queryParam("name", "John")
				.request().put(jsonEntity("{\"age\":28,\"description\":\"zzz\"}"));

		assertThat(response,
				okAndHasData(1, "[{\"id\":{\"age\":28,\"name\":\"John\"},\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}]"));

		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e21 WHERE age = 28 AND description = 'zzz'"));
	}

	@Test
	public void test_PUT_SeveralExistingObjects_MultiId() throws WebApplicationException, IOException {
		insert("e21", "age, name", "18, 'John'");
		insert("e21", "age, name", "18, 'John'");

		Response response = target("/e21/byid").queryParam("age", 18).queryParam("name", "John")
				.request().put(jsonEntity("{\"age\":28,\"description\":\"zzz\"}"));

		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID '{name:John,age:18}' and entity 'E21'\"}",
                response.readEntity(String.class));
	}
}
