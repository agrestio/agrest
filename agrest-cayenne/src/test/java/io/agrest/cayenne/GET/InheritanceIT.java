package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.inheritance.Ie1Sub1;
import io.agrest.cayenne.cayenne.inheritance.Ie1Super;
import io.agrest.cayenne.cayenne.inheritance.Ie2;
import io.agrest.cayenne.cayenne.inheritance.Ie3;
import io.agrest.cayenne.unit.inheritance.InheritanceDbTest;
import io.agrest.cayenne.unit.inheritance.InheritanceModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

public class InheritanceIT extends InheritanceDbTest {

    @BQTestTool
    static final InheritanceModelTester tester = tester(Resource.class)
            .entities(Ie1Super.class, Ie2.class, Ie3.class)
            .build();

    @Test
    public void superclass() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2", "a3")
                .values(10, 1, "v01", "v11", null, null)
                .values(20, 2, "v02", null, "v21", null)
                .values(30, 3, "v03", "v13", null, "v31")
                .exec();

        tester.target("/ie1-super")
                .get()
                .wasOk()
                .bodyEquals(3,
                        "{\"id\":10,\"a0\":\"v01\",\"a1\":\"v11\",\"type\":1}",
                        "{\"id\":20,\"a0\":\"v02\",\"a2\":\"v21\",\"type\":2}",
                        "{\"id\":30,\"a0\":\"v03\",\"a1\":\"v13\",\"a3\":\"v31\",\"type\":3}");
    }

    @Test
    public void superclass_Includes() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2", "a3")
                .values(10, 1, "v01", "v11", null, null)
                .values(20, 2, "v02", null, "v21", null)
                .values(30, 3, "v03", "v13", null, "v31")
                .exec();

        tester.target("/ie1-super")
                // super and sub attributes should be supported
                .queryParam("include", "id", "a0", "a1", "a3")
                .get()
                .wasOk()
                .bodyEquals(3,
                        "{\"id\":10,\"a0\":\"v01\",\"a1\":\"v11\"}",
                        "{\"id\":20,\"a0\":\"v02\"}",
                        "{\"id\":30,\"a0\":\"v03\",\"a1\":\"v13\",\"a3\":\"v31\"}");
    }

    @Test
    public void superclass_Includes_Relationships() {

        tester.ie2().insertColumns("id")
                .values(1)
                .values(2)
                .exec();

        tester.ie1().insertColumns("id", "type", "e2_id")
                .values(10, 1, 2)
                .values(20, 2, null)
                .exec();

        tester.ie3().insertColumns("id", "e1_id")
                .values(1, 10)
                .values(2, 20)
                .exec();


        tester.target("/ie1-super")
                // super and sub relationships should be supported
                .queryParam("include", "id", "ie2", "ie3s")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":10,\"ie2\":{\"id\":2},\"ie3s\":[{\"id\":1}]}",
                        "{\"id\":20,\"ie3s\":[{\"id\":2}]}");
    }

    @Test
    public void superclass_Excludes() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2", "a3")
                .values(10, 1, "v01", "v11", null, null)
                .values(20, 2, "v02", null, "v21", null)
                .values(30, 3, "v03", "v13", null, "v31")
                .exec();

        tester.target("/ie1-super")
                // super and sub attributes should be supported
                .queryParam("exclude", "a0", "a1", "a3")
                .get()
                .wasOk()
                .bodyEquals(3,
                        "{\"id\":10,\"type\":1}",
                        "{\"id\":20,\"a2\":\"v21\",\"type\":2}",
                        "{\"id\":30,\"type\":3}");
    }

    @Test
    public void subclass() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2")
                .values(10, 1, "v01", "v11", null)
                .values(20, 2, "v02", null, "v21")
                .exec();

        tester.target("/ie1-sub1")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":10,\"a0\":\"v01\",\"a1\":\"v11\",\"type\":1}");
    }

    @Test
    public void relatedSuperclass() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2", "a3")
                .values(10, 1, "v01", "v11", null, null)
                .values(20, 2, "v02", null, "v21", null)
                .values(30, 3, "v03", "v13", null, "v31")
                .exec();

        tester.ie3().insertColumns("id", "e1_id")
                .values(1, 10)
                .values(2, 20)
                .values(3, 30)
                .exec();

        tester.target("/ie3")
                .queryParam("include", "id", "ie1")
                .get()
                .wasOk()
                .bodyEquals(3,
                        "{\"id\":1,\"ie1\":{\"id\":10,\"a0\":\"v01\",\"a1\":\"v11\",\"type\":1}}",
                        "{\"id\":2,\"ie1\":{\"id\":20,\"a0\":\"v02\",\"a2\":\"v21\",\"type\":2}}",
                        "{\"id\":3,\"ie1\":{\"id\":30,\"a0\":\"v03\",\"a1\":\"v13\",\"a3\":\"v31\",\"type\":3}}");
    }

    @Test
    public void relatedSuperclass_IncludeRelationships() {

        tester.ie2().insertColumns("id")
                .values(1)
                .values(2)
                .exec();

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2", "a3", "e2_id")
                .values(10, 1, "v01", "v11", null, null, 2)
                .values(20, 2, "v02", null, "v21", null, null)
                .values(30, 3, "v03", "v13", null, "v31", 1)
                .exec();

        tester.ie3().insertColumns("id", "e1_id")
                .values(1, 10)
                .values(2, 20)
                .values(3, 30)
                .exec();

        tester.target("/ie3")
                .queryParam("include", "id", "ie1.type", "ie1.ie2.id", "ie1.ie3s.id")
                // adding an "exp" caused issue #590
                .queryParam("exp", "id > 0")
                .get()
                .wasOk()
                .bodyEquals(3,
                        "{\"id\":1,\"ie1\":{\"ie2\":{\"id\":2},\"ie3s\":[{\"id\":1}],\"type\":1}}",
                        "{\"id\":2,\"ie1\":{\"ie3s\":[{\"id\":2}],\"type\":2}}",
                        "{\"id\":3,\"ie1\":{\"ie2\":{\"id\":1},\"ie3s\":[{\"id\":3}],\"type\":3}}");
    }

    @Test
    public void relatedSubclass() {

        tester.ie2().insertColumns("id")
                .values(1)
                .values(2)
                .exec();

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2", "e2_id")
                .values(10, 1, "v01", "v10", null, 1)
                .values(15, 1, "v02", "v15", null, 2)
                .values(20, 2, "v03", null, "v2", null)
                .exec();

        tester.target("/ie2")
                .queryParam("include", "id", "ie1s")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":1,\"ie1s\":[{\"id\":10,\"a0\":\"v01\",\"a1\":\"v10\",\"type\":1}]}",
                        "{\"id\":2,\"ie1s\":[{\"id\":15,\"a0\":\"v02\",\"a1\":\"v15\",\"type\":1}]}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("ie1-super")
        public DataResponse<Ie1Super> getIe1Super(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie1Super.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("ie1-sub1")
        public DataResponse<Ie1Sub1> getIe1Sub1(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie1Sub1.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("ie2")
        public DataResponse<Ie2> getIE2(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie2.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("ie3")
        public DataResponse<Ie3> getIE3(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie3.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }
    }
}
