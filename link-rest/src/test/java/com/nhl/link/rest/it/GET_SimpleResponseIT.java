package com.nhl.link.rest.it;

import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import org.apache.cayenne.DataChannel;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.map.EntityResolver;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GET_SimpleResponseIT extends JerseyTest {

	public GET_SimpleResponseIT() {
		super(new InMemoryTestContainerFactory());
	}

	@Override
	public Application configure() {

		EntityResolver mockResolver = mock(EntityResolver.class);
		DataChannel mockChannel = mock(DataChannel.class);
		when(mockChannel.getEntityResolver()).thenReturn(mockResolver);

		ServerRuntime runtime = mock(ServerRuntime.class);
		when(runtime.getChannel()).thenReturn(mockChannel);

		Feature lrFeature = LinkRestBuilder.build(runtime);

		Feature feature = new Feature() {

			@Override
			public boolean configure(FeatureContext context) {
				context.register(SimpleResponseResource.class);
				return true;
			}
		};

		return new ResourceConfig().register(feature).register(lrFeature);
	}

	@Test
	public void testWrite() {

		Response response1 = target("/simple").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"message\":\"Hi!\"}", response1.readEntity(String.class));

		Response response2 = target("/simple/2").request().get();
		assertEquals(Status.OK.getStatusCode(), response2.getStatus());
		assertEquals("{\"success\":false,\"message\":\"Hi2!\"}", response2.readEntity(String.class));
	}

	@Path("simple")
	public static class SimpleResponseResource {

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
