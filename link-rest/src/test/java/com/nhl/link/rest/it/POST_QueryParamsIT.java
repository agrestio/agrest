package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E20;
import com.nhl.link.rest.it.fixture.cayenne.E21;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.runtime.query.Exclude;
import com.nhl.link.rest.runtime.query.Include;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class POST_QueryParamsIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testPost_SingleId() {

        Response response1 = target("/single-id")
                .queryParam("exclude", "age", "description")
                .request()
                .post(Entity.json("{\"id\":\"John\"}"));
        assertEquals(Response.Status.CREATED.getStatusCode(), response1.getStatus());

        E20 e20 = ObjectSelect.query(E20.class).selectFirst(newContext());
        assertNotNull(e20);
        assertEquals("John", e20.getName());

        assertEquals("{\"data\":[{\"id\":\"John\",\"name\":\"John\"}],\"total\":1}",
                response1.readEntity(String.class));

        Response response2 = target("/single-id").queryParam("exclude", "age", "description").request().post(
                Entity.json("{\"id\":\"John\"}"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response2.getStatus());
        assertTrue(response2.readEntity(String.class).contains("object already exists"));
    }

    @Test
    public void testPost_MultiId() {

        Response response1 = target("/multi-id").queryParam("exclude", "description").request().post(
                Entity.json("{\"id\":{\"age\":18,\"name\":\"John\"}}"));
        assertEquals(Response.Status.CREATED.getStatusCode(), response1.getStatus());

        E21 e21 = ObjectSelect.query(E21.class).selectFirst(newContext());
        assertNotNull(e21);
        assertEquals(Integer.valueOf(18), e21.getAge());
        assertEquals("John", e21.getName());

        assertEquals("{\"data\":[{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}],\"total\":1}",
                response1.readEntity(String.class));

        Response response2 = target("/multi-id").queryParam("exclude", "description").request().post(
                Entity.json("{\"id\":{\"age\":18,\"name\":\"John\"}}"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response2.getStatus());
        assertTrue(response2.readEntity(String.class).contains("object already exists"));
    }

    @Test
    public void testPost_ToMany() {

        insert("e3", "id, name", "1, 'xxx'");
        insert("e3", "id, name", "8, 'yyy'");

        Response response = target("/e2")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request()
                .post(Entity.json("{\"e3s\":[1,8],\"name\":\"MM\"}"));

        E2 e2 = (E2) Cayenne.objectForQuery(newContext(), new SelectQuery<>(E2.class));
        int id = Cayenne.intPKForObject(e2);

        onResponse(response)
                .statusEquals(Response.Status.CREATED)
                .bodyEquals(1, "{\"id\":" + id + ",\"e3s\":[{\"id\":1},{\"id\":8}],\"name\":\"MM\"}");

        assertEquals(2, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE e2_id = " + id));
    }


    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


        @POST
        @Path("single-id")
        public DataResponse<E20> createE20(EntityUpdate<E20> update, @QueryParam("exclude") List<Exclude> exclude) {

            return LinkRest
                    .create(E20.class, config)
                    .exclude(exclude)
                    .syncAndSelect(update);
        }

        @POST
        @Path("multi-id")
        public DataResponse<E21> createE21(EntityUpdate<E21> update, @QueryParam("exclude") List<Exclude> exclude) {


            return LinkRest
                    .create(E21.class, config)
                    .exclude(exclude)
                    .syncAndSelect(update);
        }

        @POST
        @Path("e2")
        public DataResponse<E2> createE2(String targetData,
                                         @QueryParam("include") List<Include> include,
                                         @QueryParam("exclude") List<Exclude> exclude) {

            return LinkRest
                    .create(E2.class, config)
                    .include(include)
                    .exclude(exclude)
                    .syncAndSelect(targetData);
        }

    }

}
