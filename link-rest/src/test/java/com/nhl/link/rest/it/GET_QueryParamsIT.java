package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E20;
import com.nhl.link.rest.it.fixture.cayenne.E21;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GET_QueryParamsIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void test_SelectById() {

        newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e20 (name) values ('John')"));

        Response response1 = target("/single-id/John").queryParam("exclude", "age", "description").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":\"John\",\"name\":\"John\"}],\"total\":1}",
                response1.readEntity(String.class));

        newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e20 (name) values ('John')"));

        Response response2 = target("/single-id/John").queryParam("exclude", "age", "description").request().get();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID 'John' and entity 'E20'\"}",
                response2.readEntity(String.class));
    }

    @Test
    public void test_SelectById_MultiId() {

        newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e21 (age, name) values (18, 'John')"));

        Response response1 = target("/multi-id/byid")
                .queryParam("age", 18).queryParam("name", "John")
                .queryParam("exclude", "description").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}],\"total\":1}",
                response1.readEntity(String.class));

        newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e21 (age, name) values (18, 'John')"));

        Response response2 = target("/multi-id/byid")
                .queryParam("age", 18).queryParam("name", "John")
                .queryParam("exclude", "description").request().get();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID '{name:John,age:18}' and entity 'E21'\"}",
                response2.readEntity(String.class));
    }

    @Test
    public void test_Sort_ById() {

        insert("e4", "id", "2");
        insert("e4", "id", "1");
        insert("e4", "id", "3");

        Response response = target("/e4")
                .queryParam("sort", urlEnc("[{\"property\":\"id\",\"direction\":\"DESC\"}]"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(response).bodyEquals(3, "{\"id\":3}", "{\"id\":2}", "{\"id\":1}");
    }

    @Test
    public void test_Sort_Invalid() {

        Response response = target("/e4")
                .queryParam("sort", urlEnc("[{\"property\":\"xyz\",\"direction\":\"DESC\"}]"))
                .queryParam("include", "id")
                .request()
                .get();

        onResponse(response)
                .statusEquals(Response.Status.BAD_REQUEST)
                .bodyEquals("{\"success\":false,\"message\":\"Invalid path 'xyz' for 'E4'\"}");
    }

    @Test
    public void test_SelectById_Params() {

        insert("e4", "id", "2");

        Response response1 = target("/e4/2").request().get();
        onSuccess(response1).bodyEquals(1, "{\"id\":2,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
                + "\"cInt\":null,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":null}");

        Response response2 = target("/e4/2").queryParam("include", "id").request().get();
        onSuccess(response2).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void test_Select_MapByRootEntity_Related() {

        insert("e2", "id, name", "1, 'zzz'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "8,  1, 'aaa'");
        insert("e3", "id, e2_id, name", "9,  1, 'bbb'");
        insert("e3", "id, e2_id, name", "10, 2, 'ccc'");

        Response response = target("/e3")
                .queryParam("mapBy", E3.E2.dot(E2.ID_PK_COLUMN).getName())
                .queryParam("exclude", E3.PHONE_NUMBER.getName())
                .request()
                .get();

        onSuccess(response).bodyEqualsMapBy(3,
                "\"1\":[{\"id\":8,\"name\":\"aaa\"},{\"id\":9,\"name\":\"bbb\"}]",
                "\"2\":[{\"id\":10,\"name\":\"ccc\"}]");
    }

    @Test
    public void test_CayenneExp_Map_Params() {

        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

        Response r1 = target("/e2").queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"name = $n\",\"params\":{\"n\":\"xxx\"}}")).request().get();

        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":1}],\"total\":1}", r1.readEntity(String.class));
    }

    @Test
    public void test_ToMany_Sort() {

        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values "
                        + "(8, 1, 'z'),(9, 1, 's'),(7, 1, 'b')"));

        Response response1 = target("/e2").queryParam("include", urlEnc("{\"path\":\"e3s\",\"sort\":\"name\"}"))
                .queryParam("include", "id").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":["
                + "{\"id\":7,\"name\":\"b\",\"phoneNumber\":null}," + "{\"id\":9,\"name\":\"s\",\"phoneNumber\":null},"
                + "{\"id\":8,\"name\":\"z\",\"phoneNumber\":null}]}],\"total\":1}", response1.readEntity(String.class));
    }

    @Test
    public void test_ToMany_SortPath() {

        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e5 (id,name) values (145, 'B'),(146, 'A')"));
        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (11, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, e5_id, name) "
                        + "values (18, 11, 145, 's'),(19, 11, 145, 'z'),(17, 11, 146, 'b')"));

        Response response1 = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"sort\":[{\"property\":\"e5.name\"}]}"))
                .queryParam("include", "id").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":11,\"e3s\":["
                + "{\"id\":17,\"name\":\"b\",\"phoneNumber\":null},"
                + "{\"id\":18,\"name\":\"s\",\"phoneNumber\":null},"
                + "{\"id\":19,\"name\":\"z\",\"phoneNumber\":null}]}],\"total\":1}", response1.readEntity(String.class));
    }

    @Test
    public void test_ToMany_SortPath_Dir() {

        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e5 (id,name) values (245, 'B'),(246, 'A')"));
        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (21, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, e5_id, name) "
                        + "values (28, 21, 245, 's'),(29, 21, 245, 'z'),(27, 21, 246, 'b')"));

        Response response1 = target("/e2")
                .queryParam(
                        "include",
                        urlEnc("{\"path\":\"e3s\",\"sort\":[{\"property\":\"e5.name\", \"direction\":\"DESC\"},{\"property\":\"name\", \"direction\":\"DESC\"}]}"))
                .queryParam("include", "id").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":21,\"e3s\":["
                + "{\"id\":29,\"name\":\"z\",\"phoneNumber\":null},"
                + "{\"id\":28,\"name\":\"s\",\"phoneNumber\":null},"
                + "{\"id\":27,\"name\":\"b\",\"phoneNumber\":null}]}],\"total\":1}", response1.readEntity(String.class));
    }

    @Test
    public void test_Select_Prefetching_StartLimit() {

        insert("e2", "id, name", "1, 'xxx'");

        insert("e3", "id, name, e2_id", "8, 'yyy', 1");
        insert("e3", "id, name, e2_id", "9, 'zzz', 1");
        insert("e3", "id, name, e2_id", "10, 'zzz', 1");
        insert("e3", "id, name, e2_id", "11, 'zzz', 1");

        Response response = target("/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("start", "1")
                .queryParam("limit", "2")
                .request()
                .get();

        onSuccess(response).bodyEquals(4,
                "{\"id\":9,\"e2\":{\"id\":1}}",
                "{\"id\":10,\"e2\":{\"id\":1}}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


        @GET
        @Path("single-id/{id}")
        public DataResponse<E20> getE20ById(@PathParam("id") String name, @QueryParam("exclude") List<String> exclude) {

            return LinkRest.service(config)
                    .select(E20.class)
                    .byId(name)
                    .exclude(exclude)
                    .getOne();
        }

        @GET
        @Path("multi-id/byid")
        public DataResponse<E21> getE21ById(@QueryParam("age") int age,
                                            @QueryParam("name") String name,
                                            @QueryParam("exclude") List<String> exclude) {

            Map<String, Object> id = new HashMap<>(3);
            id.put("age", age);
            id.put("name", name);

            return LinkRest.service(config)
                    .select(E21.class)
                    .byId(id)
                    .exclude(exclude)
                    .getOne();
        }

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@QueryParam("include") List<String> include,
                                      @QueryParam("sort") List<String> sort) {

            return LinkRest.service(config)
                    .select(E4.class)
                    .sort(sort)
                    .include(include)
                    .get();
        }

        @GET
        @Path("e4/{id}")
        public DataResponse<E4> getE4_WithIncludeExclude(@PathParam("id") int id,
                                                         @QueryParam("include") List<String> include) {

            return LinkRest.service(config)
                    .select(E4.class)
                    .byId(id)
                    .include(include)
                    .getOne();
        }

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@QueryParam("sort")  List<String> sort,
                                      @QueryParam("start") int start,
                                      @QueryParam("limit") int limit,
                                      @QueryParam("mapBy") String mapBy,
                                      @QueryParam("include") List<String> include,
                                      @QueryParam("exclude") List<String> exclude) {
            return LinkRest.service(config)
                    .select(E3.class)
                    .sort(sort)
                    .fetchOffset(start)
                    .fetchLimit(limit)
                    .mapBy(mapBy)
                    .include(include)
                    .exclude(exclude)
                    .get();
        }

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(@QueryParam("include") List<String> include,
                                      @QueryParam("cayenneExp") String cayenneExp) {

            return LinkRest.service(config)
                    .select(E2.class)
                    .include(include)
                    .cayenneExp(cayenneExp)
                    .get();
        }
    }
}
