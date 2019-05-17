package io.agrest.it;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class POST_AgRequestIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E3.class};
    }

    @Test
    public void testIncludes_OverriddenByAgRequest() {

        Response r = target("/e3_includes")
                .queryParam("include", "id")
                .request()
                .post(Entity.json("[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"}]"));

        onResponse(r)
                .wasCreated()
                .bodyEquals(3, "{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"}");
    }

    @Test
    public void testExcludes_OverriddenByAgRequest() {

        Response r = target("/e3_excludes")
                .queryParam("exclude", "name")
                .request()
                .post(Entity.json("[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"}]"));

        onResponse(r)
                .wasCreated()
                .bodyEquals(3,
                        "{\"name\":\"aaa\",\"phoneNumber\":null}",
                        "{\"name\":\"zzz\",\"phoneNumber\":null}",
                        "{\"name\":\"bbb\",\"phoneNumber\":null}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


        @POST
        @Path("e3_includes")
        public DataResponse<E3> create_includes(@Context UriInfo uriInfo, String requestBody) {

            AgRequest agRequest = Ag.request(config).addInclude("name").build();

            return Ag.create(E3.class, config)
                    .uri(uriInfo)
                    .request(agRequest) // overrides parameters from uriInfo
                    .syncAndSelect(requestBody);
        }

        @POST
        @Path("e3_excludes")
        public DataResponse<E3> create_excludes(@Context UriInfo uriInfo, String requestBody) {

            AgRequest agRequest = Ag.request(config).addExclude("id").build();

            return Ag.create(E3.class, config)
                    .uri(uriInfo)
                    .request(agRequest) // overrides parameters from uriInfo
                    .syncAndSelect(requestBody);
        }

    }

}
