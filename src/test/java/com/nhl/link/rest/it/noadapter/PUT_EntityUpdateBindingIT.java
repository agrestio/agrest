package com.nhl.link.rest.it.noadapter;

import static com.nhl.link.rest.unit.matcher.LRMatchers.okAndHasBody;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.resource.E3Resource;

public class PUT_EntityUpdateBindingIT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E3Resource.class);
	}

	@Test
	public void testPut_ToOne() throws WebApplicationException, IOException {
		insert("e2", "id, name", "1, 'xxx'");
		insert("e2", "id, name", "8, 'yyy'");
		insert("e3", "id, name, e2_id", "3, 'zzz', 8");

		Response response = target("/e3/updatebinding/3").request().put(jsonEntity("{\"id\":3,\"e2\":1}"));
		assertThat(response, okAndHasBody("{\"success\":true}"));
		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id = 1"));
	}
}
