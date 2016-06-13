package com.nhl.link.rest.it.pojo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import com.nhl.link.rest.it.fixture.pojo.JerseyTestOnPojo;
import com.nhl.link.rest.it.fixture.pojo.model.P1;
import com.nhl.link.rest.it.fixture.pojo.model.P3;
import com.nhl.link.rest.it.fixture.pojo.model.P4;
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
		assertEquals("{\"data\":[{\"id\":\"o2id\",\"intProp\":16}],\"total\":1}", response1.readEntity(String.class));
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

		Response response1 = target("/pojo/p6").queryParam("sort", "id").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":\"o1id\",\"intProp\":15}," + "{\"id\":\"o2id\",\"intProp\":16}],\"total\":2}",
				response1.readEntity(String.class));
	}

	@Test
	public void test_SelectAll_IncludeToOne() throws WebApplicationException, IOException {

		P3 o0 = new P3();
		o0.setName("xx3");

		P4 o1 = new P4();
		o1.setP3(o0);

		pojoDB.bucketForType(P4.class).put("o1id", o1);

		Response response1 = target("/pojo/p4").queryParam("include", "p3").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"p3\":{\"name\":\"xx3\"}}],\"total\":1}", response1.readEntity(String.class));
	}

	@Test
	public void test_SelectAll_NoId() throws WebApplicationException, IOException {

		P1 o1 = new P1();
		o1.setName("n2");
		P1 o2 = new P1();
		o2.setName("n1");
		pojoDB.bucketForType(P1.class).put("o1id", o1);
		pojoDB.bucketForType(P1.class).put("o2id", o2);

		Response response1 = target("/pojo/p1").queryParam("sort", "name").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"name\":\"n1\"}," + "{\"name\":\"n2\"}],\"total\":2}",
				response1.readEntity(String.class));
	}

	@Test
	public void test_SelectAll_MapBy() {

		P1 o1 = new P1();
		o1.setName("n2");
		P1 o2 = new P1();
		o2.setName("n1");
		pojoDB.bucketForType(P1.class).put("o1id", o1);
		pojoDB.bucketForType(P1.class).put("o2id", o2);

		Response response1 = target("/pojo/p1").queryParam("mapBy", "name").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":{\"n1\":[{\"name\":\"n1\"}],\"n2\":[{\"name\":\"n2\"}]},\"total\":2}",
				response1.readEntity(String.class));
	}
}
