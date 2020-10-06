package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E3;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class POST_AgRequestIT extends JerseyAndDerbyCase {

    @BQTestTool
    static final CayenneAgTester tester = tester(Resource.class)

            .entities(E3.class)
            .build();

    @Test
    public void testIncludes_OverriddenByAgRequest() {

        tester.target("/e3_includes")
                .queryParam("include", "id")
                .post("[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"}]")
                .wasCreated()
                .bodyEquals(3, "{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"}");
    }

    @Test
    public void testExcludes_OverriddenByAgRequest() {

        tester.target("/e3_excludes")
                .queryParam("exclude", "name")
                .post("[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"}]")
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
