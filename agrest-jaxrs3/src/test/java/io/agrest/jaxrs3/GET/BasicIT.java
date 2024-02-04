package io.agrest.jaxrs3.GET;

import io.agrest.DataResponse;
import io.agrest.jaxrs3.AgJaxrs;
import io.agrest.jaxrs3.junit.AgPojoTester;
import io.agrest.jaxrs3.junit.PojoTest;
import io.agrest.jaxrs3.junit.pojo.P1;
import io.agrest.jaxrs3.junit.pojo.P10;
import io.agrest.jaxrs3.junit.pojo.P3;
import io.agrest.jaxrs3.junit.pojo.P4;
import io.agrest.jaxrs3.junit.pojo.P6;
import io.agrest.jaxrs3.junit.pojo.P8;
import io.agrest.jaxrs3.junit.pojo.P9;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BasicIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = PojoTest.tester(Resource.class).build();

    @Test
    public void byId() {

        P6 o1 = new P6();
        o1.setIntProp(15);
        o1.setStringId("o1id");
        P6 o2 = new P6();
        o2.setIntProp(16);
        o2.setStringId("o2id");
        tester.p6().put("o1id", o1);
        tester.p6().put("o2id", o2);

        tester.target("/pojo/p6/o2id").get()
                .wasOk()
                .bodyEquals(1, "{\"id\":\"o2id\",\"intProp\":16}");
    }

    @Test
    public void byId_MultiKey() {

        P10 o1 = new P10();
        o1.setId1(5);
        o1.setId2("six");
        o1.setA1("seven");

        P10 o2 = new P10();
        o2.setId1(8);
        o2.setId2("nine");
        o2.setA1("ten");

        tester.p10().put(o1.id(), o1);
        tester.p10().put(o2.id(), o2);

        tester.target("/pojo/p10/5/six").get()
                .wasOk()
                .bodyEquals(1, "{\"id\":{\"id1\":5,\"id2\":\"six\"},\"a1\":\"seven\"}");
    }


    @Test
    public void basic() {

        P6 o1 = new P6();
        o1.setIntProp(15);
        o1.setStringId("o1id");
        P6 o2 = new P6();
        o2.setIntProp(16);
        o2.setStringId("o2id");
        tester.p6().put("o1id", o1);
        tester.p6().put("o2id", o2);

        tester.target("/pojo/p6").queryParam("sort", "id")
                .get()
                .wasOk()
                .bodyEquals(2, "{\"id\":\"o1id\",\"intProp\":15},{\"id\":\"o2id\",\"intProp\":16}");
    }

    @Test
    public void includeToOne() {

        P3 o0 = new P3();
        o0.setName("xx3");

        P4 o1 = new P4();
        o1.setP3(o0);

        tester.p4().put("o1id", o1);

        tester.target("/pojo/p4").queryParam("include", "p3").get()
                .wasOk()
                .bodyEquals(1, "{\"p3\":{\"name\":\"xx3\"}}");
    }

    @Test
    public void noId() {

        P1 o1 = new P1();
        o1.setName("n2");
        P1 o2 = new P1();
        o2.setName("n1");
        tester.p1().put("o1id", o1);
        tester.p1().put("o2id", o2);

        tester.target("/pojo/p1").queryParam("sort", "name").get()
                .wasOk()
                .bodyEquals(2, "{\"name\":\"n1\"},{\"name\":\"n2\"}");
    }

    @Test
    public void withTime() {

        P9 o9 = new P9();
        o9.setName("p9name1");
        LocalDateTime ldt = LocalDateTime.of(1999, 10, 2, 12, 54, 31);
        o9.setCreated(OffsetDateTime.of(ldt, ZoneOffset.ofHours(3)));
        o9.setCreatedLocal(ldt);
        tester.p9().put("o9id", o9);

        tester.target("/pojo/p9").get()
                .wasOk()
                .bodyEquals(1, "{\"created\":\"1999-10-02T12:54:31+03:00\",\"createdLocal\":\"1999-10-02T12:54:31\",\"name\":\"p9name1\"}");
    }

    @Test
    public void mapBy() {

        P1 o1 = new P1();
        o1.setName("n2");
        P1 o2 = new P1();
        o2.setName("n1");
        tester.p1().put("o1id", o1);
        tester.p1().put("o2id", o2);

        tester.target("/pojo/p1").queryParam("mapBy", "name").get()
                .wasOk()
                .bodyEqualsMapBy(2, "\"n1\":[{\"name\":\"n1\"}],\"n2\":[{\"name\":\"n2\"}]");
    }

    @Test
    public void collectionAttributes() {

        P8 o1 = new P8();
        o1.setId(1);
        o1.setBooleans(List.of(true, false));
        o1.setCharacters(List.of('a', 'b', 'c'));
        o1.setDoubles(List.of(1., 2.5, 3.5));
        o1.setStringSet(Set.of("abc"));

        List<Number> numbers = Arrays.asList((byte) 0, (short) 1, 2, 3L, 4.f, 5.);
        o1.setNumberList(numbers);
        o1.setWildcardCollection(numbers);

        tester.p8().put(1, o1);

        tester.target("/pojo/p8/1").get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1," +
                        "\"booleans\":[true,false]," +
                        "\"characters\":[\"a\",\"b\",\"c\"]," +
                        "\"doubles\":[1.0,2.5,3.5]," +
                        "\"genericCollection\":[]," +
                        "\"numberList\":[0,1,2,3,4.0,5.0]," +
                        "\"stringSet\":[\"abc\"]," +
                        "\"wildcardCollection\":[0,1,2,3,4.0,5.0]}");
    }

    @Test
    public void getEmpty() {

        P1 o1 = new P1();
        o1.setName("n2");
        tester.p1().put("o1id", o1);

        tester.target("/pojo/p1_empty")
                .get()
                .wasOk()
                .bodyEquals("{\"data\":[],\"total\":0}");
    }

    @Path("pojo")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("p1_empty")
        public DataResponse<P1> p1Empty(@Context UriInfo uriInfo) {
            return AgJaxrs.select(P1.class, config).getEmpty();
        }

        @GET
        @Path("p1")
        public DataResponse<P1> p1All(@Context UriInfo uriInfo) {
            return AgJaxrs.select(P1.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("p4")
        public DataResponse<P4> p4All(@Context UriInfo uriInfo) {
            return AgJaxrs.select(P4.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("p6")
        public DataResponse<P6> p6All(@Context UriInfo uriInfo) {
            return AgJaxrs.select(P6.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("p6/{id}")
        public DataResponse<P6> p6ById(@PathParam("id") String id, @Context UriInfo uriInfo) {
            return AgJaxrs.select(P6.class, config).clientParams(uriInfo.getQueryParameters()).byId(id).get();
        }

        @GET
        @Path("p8/{id}")
        public DataResponse<P8> p8ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgJaxrs.select(P8.class, config).clientParams(uriInfo.getQueryParameters()).byId(id).get();
        }

        @GET
        @Path("p9")
        public DataResponse<P9> p9All(@Context UriInfo uriInfo) {
            return AgJaxrs.select(P9.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("p10/{id1}/{id2}")
        public DataResponse<P10> p10ById(@PathParam("id1") int id1, @PathParam("id2") String id2, @Context UriInfo uriInfo) {
            return AgJaxrs.select(P10.class, config).clientParams(uriInfo.getQueryParameters()).byId(P10.id(id1, id2)).get();
        }
    }
}