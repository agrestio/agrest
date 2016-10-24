package com.nhl.link.rest.it.sencha;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.resource.E17Resource;
import com.nhl.link.rest.it.fixture.resource.E2Resource;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import com.nhl.link.rest.runtime.adapter.sencha.SenchaAdapter;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Sencha_DELETE_IT extends JerseyTestOnDerby {

    @Override
	protected void doAddResources(FeatureContext context) {
		context.register(E2Resource.class);
        context.register(E17Resource.class);
	}

	@Override
	protected LinkRestBuilder doConfigure() {
		return super.doConfigure().adapter(new SenchaAdapter());
	}

    @Test
	public void test_BatchDelete() throws WebApplicationException, IOException {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e2", "id, name", "3, 'zzz'");

		Response response1 = target("/e2").request()
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .method("DELETE", Entity.entity(" [{\"id\":1},{\"id\":2}]",
                        MediaType.APPLICATION_JSON), Response.class);

		assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e2"));
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e2 WHERE id = 3"));
	}

    @Test
    public void test_BatchDelete_CompoundId() throws WebApplicationException, IOException {

        insert("e17", "id1, id2, name", "1, 1, 'aaa'");
        insert("e17", "id1, id2, name", "2, 2, 'bbb'");
        insert("e17", "id1, id2, name", "3, 3, 'ccc'");

        Response response1 = target("/e17/batch_delete").request()
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .method("DELETE", Entity.entity("[{\"id1\":1,\"id2\":1},{\"id1\":2,\"id2\":2}]",
                        MediaType.APPLICATION_JSON), Response.class);

		assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
		assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e17"));
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e17 WHERE id1 = 3 AND id2 = 3"));
    }
}
