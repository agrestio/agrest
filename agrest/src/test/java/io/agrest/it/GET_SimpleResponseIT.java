package io.agrest.it;

import io.agrest.SimpleResponse;
import io.agrest.it.fixture.JerseyAndPojoCase;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

public class GET_SimpleResponseIT extends JerseyAndPojoCase {

	@BeforeClass
	public static void startTestRuntime() {
		startTestRuntime(Resource.class);
	}

	@Test
	public void testWrite() {

		Response r1 = target("/simple").request().get();
		onSuccess(r1).bodyEquals("{\"success\":true,\"message\":\"Hi!\"}");

		Response r2 = target("/simple/2").request().get();
		onSuccess(r2).bodyEquals("{\"success\":false,\"message\":\"Hi2!\"}");
	}

	@Path("simple")
	public static class Resource {

		@GET
		public SimpleResponse get() {
			return new SimpleResponse(true, "Hi!");
		}

		@GET
		@Path("2")
		public SimpleResponse get2() {
			return new SimpleResponse(false, "Hi2!");
		}
	}
}
