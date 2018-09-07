package io.agrest.it;

import io.agrest.AgRESTException;
import io.agrest.runtime.AgRESTBuilder;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GET_ExceptionIT extends JerseyTest {

	public GET_ExceptionIT() {
		super(new InMemoryTestContainerFactory());
	}

	@Override
	public Application configure() {

		EntityResolver mockResolver = mock(EntityResolver.class);
		DataChannel mockChannel = mock(DataChannel.class);
		when(mockChannel.getEntityResolver()).thenReturn(mockResolver);

		ServerRuntime runtime = mock(ServerRuntime.class);
		when(runtime.getChannel()).thenReturn(mockChannel);

		Feature agFeature = AgRESTBuilder.build(runtime);

		Feature testFeature = new Feature() {

			@Override
			public boolean configure(FeatureContext context) {
				context.register(ExceptionResource.class);
				return true;
			}
		};

		return new ResourceConfig().register(testFeature).register(agFeature);
	}

	@Test
	public void testNoData() {

		Response response = target("/nodata").request().get();

		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		assertEquals("{\"success\":false,\"message\":\"request failed\"}", response.readEntity(String.class));
	}

	@Test
	public void testNoData_WithThrowable() {
		Response response = target("/nodata/th").request().get();

		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		assertEquals("{\"success\":false,\"message\":\"request failed with th\"}", response.readEntity(String.class));
	}

	@Path("nodata")
	public static class ExceptionResource {

		@GET
		public Response get() {
			throw new AgRESTException(Status.NOT_FOUND, "request failed");
		}

		@GET
		@Path("th")
		public Response getTh() {
			try {
				throw new Throwable("Dummy");
			} catch (Throwable th) {
				throw new AgRESTException(Status.INTERNAL_SERVER_ERROR, "request failed with th", th);
			}
		}
	}

}
