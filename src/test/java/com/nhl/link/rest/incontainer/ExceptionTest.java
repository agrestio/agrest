package com.nhl.link.rest.incontainer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.configuration.server.ServerRuntime;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.junit.Test;

import com.nhl.link.rest.runtime.LinkRestBuilder;
import com.nhl.link.rest.unit.resource.ExceptionResource;

public class ExceptionTest extends JerseyTest {

	public ExceptionTest() {
		super(new InMemoryTestContainerFactory());
	}

	@Override
	public Application configure() {

		Feature lrFeature = new LinkRestBuilder().cayenneRuntime(mock(ServerRuntime.class)).build().getFeature();

		Feature testFeature = new Feature() {

			@Override
			public boolean configure(FeatureContext context) {
				context.register(ExceptionResource.class);
				return true;
			}
		};

		return new ResourceConfig().register(testFeature).register(lrFeature);
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

}
