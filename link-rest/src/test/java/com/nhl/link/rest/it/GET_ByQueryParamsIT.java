package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E20;
import com.nhl.link.rest.it.fixture.cayenne.E21;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GET_ByQueryParamsIT extends JerseyTestOnDerby {

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
    public void test_SelectById_Params() {

        insert("e4", "id", "2");

        Response response1 = target("/e4/2").request().get();
        onSuccess(response1).bodyEquals(1, "{\"id\":2,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
                + "\"cInt\":null,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":null}");

        Response response2 = target("/e4/2").queryParam("include", "id").request().get();
        onSuccess(response2).bodyEquals(1, "{\"id\":2}");
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

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;
        @Context
        private UriInfo uriInfo;
        @Context
        ContainerRequestContext requestContext;



        @GET
        @Path("single-id/{id}")
        public DataResponse<E20> getE20ById(@PathParam("id") String name, @QueryParam("exclude") List<String> exclude) {

            Map<String, List<String>>  queryParams = new HashMap<>();
            queryParams.put("exclude",  exclude);

            return LinkRest.service(config)
                    .select(E20.class)
                    .byId(name)
                    .queryParams(queryParams)
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

            Map<String, List<String>>  queryParams = new HashMap<>();
            queryParams.put("age", Arrays.asList("" + age));
            queryParams.put("name", Arrays.asList(name));
            queryParams.put("exclude",  exclude);

            return LinkRest
                    .service(config)
                    .select(E21.class)
                    .byId(id)
                    .queryParams(queryParams)
                    .getOne();
        }

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@QueryParam("include") List<String> include,
                                      @QueryParam("sort") List<String> sort) {

            Map<String, List<String>>  queryParams = new HashMap<>();
            queryParams.put("include",  include);
            queryParams.put("sort",  sort);

            return LinkRest.service(config).select(E4.class).queryParams(queryParams).get();
        }

        @GET
        @Path("e4/{id}")
        public DataResponse<E4> getE4_WithIncludeExclude(@PathParam("id") int id,
                                                         @QueryParam("include") List<String> include) {

            Map<String, List<String>>  queryParams = new HashMap<>();
            queryParams.put("include",  include);

            return LinkRest.service(config)
                    .select(E4.class)
                    .byId(id)
                    .queryParams(queryParams)
                    .getOne();
        }

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(@QueryParam("include") List<String> include,
                                      @QueryParam("cayenneExp") List<String> cayenneExp) {

            Map<String, List<String>>  queryParams = new HashMap<>();
            queryParams.put("include",  include);
            queryParams.put("cayenneExp",  cayenneExp);

            return LinkRest.service(config).select(E2.class).queryParams(queryParams).get();
        }
    }
}
