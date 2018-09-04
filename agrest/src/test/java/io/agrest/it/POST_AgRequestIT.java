package io.agrest.it;

import io.agrest.AgREST;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Include;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class POST_AgRequestIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }


    @Test
    public void testPost_Includes_OverrideByLrRequest() {

        Response r2 = target("/e3_includes")
                .queryParam("include", "id")
                .request()
                .post(Entity.json("[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}]"));

        assertEquals(Status.CREATED.getStatusCode(), r2.getStatus());

        // returns names instead of id's due to overriding include by AgRequest
        assertEquals(
                "{\"data\":[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}],\"total\":4}",
                r2.readEntity(String.class));
    }

    @Test
    public void testPost_Excludes_OverrideByLrRequest() {

        Response r2 = target("/e3_excludes")
                .queryParam("exclude", "name")
                .request()
                .post(Entity.json("[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}]"));
        assertEquals(Status.CREATED.getStatusCode(), r2.getStatus());

        // returns 'name' and 'phoneNumber' fields except 'id' due to overriding exclude by AgRequest
        assertEquals(
                "{\"data\":[{\"name\":\"aaa\",\"phoneNumber\":null},{\"name\":\"zzz\",\"phoneNumber\":null},{\"name\":\"bbb\",\"phoneNumber\":null},{\"name\":\"yyy\",\"phoneNumber\":null}],\"total\":4}",
                r2.readEntity(String.class));
    }


    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


        @POST
        @Path("e3_includes")
        public DataResponse<E3> create_includes(@Context UriInfo uriInfo, String requestBody) {
            List<Include> includes = Collections.singletonList(new Include("name"));
            AgRequest agRequest = AgRequest.builder().includes(includes).build();

            return AgREST.create(E3.class, config)
                    .uri(uriInfo)
                    .request(agRequest) // overrides parameters from uriInfo
                    .syncAndSelect(requestBody);
        }

        @POST
        @Path("e3_excludes")
        public DataResponse<E3> create_excludes(@Context UriInfo uriInfo, String requestBody) {
            List<Exclude> excludes = Collections.singletonList(new Exclude("id"));
            AgRequest agRequest = AgRequest.builder().excludes(excludes).build();

            return AgREST.create(E3.class, config)
                    .uri(uriInfo)
                    .request(agRequest) // overrides parameters from uriInfo
                    .syncAndSelect(requestBody);
        }

    }

}
