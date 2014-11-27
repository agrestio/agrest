package com.nhl.link.rest.it.pojo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import com.nhl.link.rest.it.fixture.pojo.JerseyTestOnPojo;
import com.nhl.link.rest.it.fixture.pojo.model.P6;

public class PojoLinkRestService_InContainer_GET_IT extends JerseyTestOnPojo {

	@Test
	public void test_SelectById() throws WebApplicationException, IOException {

		P6 o1 = new P6();
		o1.setIntProp(15);
		o1.setStringId("o1id");
		P6 o2 = new P6();
		o2.setIntProp(16);
		o2.setStringId("o2id");
		pojoDB.bucketForType(P6.class).put("o1id", o1);
		pojoDB.bucketForType(P6.class).put("o2id", o2);

		Response response1 = target("/pojo/p6/o2id").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals(
				"{\"success\":true,\"data\":[{\"id\":\"o2id\",\"intProp\":16,\"stringId\":\"o2id\"}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void test_SelectAll() throws WebApplicationException, IOException {

		P6 o1 = new P6();
		o1.setIntProp(15);
		o1.setStringId("o1id");
		P6 o2 = new P6();
		o2.setIntProp(16);
		o2.setStringId("o2id");
		pojoDB.bucketForType(P6.class).put("o1id", o1);
		pojoDB.bucketForType(P6.class).put("o2id", o2);

		Response response1 = target("/pojo/p6").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":\"o1id\",\"intProp\":15,\"stringId\":\"o1id\"},"
				+ "{\"id\":\"o2id\",\"intProp\":16,\"stringId\":\"o2id\"}],\"total\":2}",
				response1.readEntity(String.class));
	}
}
