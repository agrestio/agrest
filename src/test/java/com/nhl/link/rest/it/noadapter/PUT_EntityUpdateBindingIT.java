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
	public void testPut_Single() throws WebApplicationException, IOException {

		insert("e3", "id, name", "3, 'zzz'");

		Response response = target("/e3/updatebinding/3").request().put(jsonEntity("{\"id\":3,\"name\":\"yyy\"}"));
		assertThat(response, okAndHasBody("{\"success\":true}"));
		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND name = 'yyy'"));
	}

	@Test
	public void testPut_Collection() throws WebApplicationException, IOException {
		insert("e3", "id, name", "3, 'zzz'");
		insert("e3", "id, name", "4, 'xxx'");
		insert("e3", "id, name", "5, 'mmm'");

		Response response = target("/e3/updatebinding").request()
				.put(jsonEntity("[{\"id\":3,\"name\":\"yyy\"},{\"id\":5,\"name\":\"nnn\"}]"));
		assertThat(response, okAndHasBody("{\"success\":true}"));

		assertEquals(2, intForQuery("SELECT COUNT(1) FROM utest.e3"));
		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND name = 'yyy'"));
		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 5 AND name = 'nnn'"));
	}
}
