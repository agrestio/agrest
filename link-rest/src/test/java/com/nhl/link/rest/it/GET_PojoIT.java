package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.pojo.JerseyTestOnPojo;
import com.nhl.link.rest.it.fixture.pojo.model.P1;
import com.nhl.link.rest.it.fixture.pojo.model.P3;
import com.nhl.link.rest.it.fixture.pojo.model.P4;
import com.nhl.link.rest.it.fixture.pojo.model.P6;
import com.nhl.link.rest.it.fixture.pojo.model.P8;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GET_PojoIT extends JerseyTestOnPojo {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(PojoResource.class);
    }

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

    @Test
    public void test_CollectionAttributes() throws WebApplicationException, IOException {

        P8 o1 = new P8();
        o1.setBooleans(Arrays.asList(true, false));
        o1.setCharacters(Arrays.asList('a', 'b', 'c'));
        o1.setDoubles(Arrays.asList(1., 2.5, 3.5));
        o1.setStringSet(Collections.singleton("abc"));

        List<Number> numbers = Arrays.asList((byte) 0, (short) 1, 2, 3L, 4.f, 5.);
        o1.setNumberList(numbers);
        o1.setWildcardCollection(numbers);

        pojoDB.bucketForType(P8.class).put(1, o1);

        Response response1 = target("/pojo/p8/1").request().get();
        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{" +
                "\"booleans\":[true,false]," +
                "\"characters\":[\"a\",\"b\",\"c\"]," +
                "\"doubles\":[1.0,2.5,3.5]," +
                "\"genericCollection\":[]," +
                "\"numberList\":[0,1,2,3,4.0,5.0]," +
                "\"stringSet\":[\"abc\"]," +
                "\"wildcardCollection\":[0,1,2,3,4.0,5.0]}],\"total\":1}", response1.readEntity(String.class));
    }

    @Path("pojo")
    public static class PojoResource {

        @Context
        private Configuration config;

        @GET
        @Path("p1")
        public DataResponse<P1> p1All(@Context UriInfo uriInfo) {
            return LinkRest.select(P1.class, config).uri(uriInfo).get();
        }

        @GET
        @Path("p4")
        public DataResponse<P4> p4All(@Context UriInfo uriInfo) {
            return LinkRest.select(P4.class, config).uri(uriInfo).get();
        }

        @GET
        @Path("p6")
        public DataResponse<P6> p6All(@Context UriInfo uriInfo) {
            return LinkRest.select(P6.class, config).uri(uriInfo).get();
        }

        @GET
        @Path("p6/{id}")
        public DataResponse<P6> p6ById(@PathParam("id") String id, @Context UriInfo uriInfo) {
            return LinkRest.service(config).selectById(P6.class, id, uriInfo);
        }

        @GET
        @Path("p8/{id}")
        public DataResponse<P8> p8ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return LinkRest.service(config).selectById(P8.class, id, uriInfo);
        }
    }
}
