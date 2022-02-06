package io.agrest.cayenne;


import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class POST_AgRequestIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
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

            AgRequest agRequest = AgJaxrs.request(config).addInclude("name").build();

            return AgJaxrs.create(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .request(agRequest) // overrides parameters from uriInfo
                    .syncAndSelect(requestBody);
        }

        @POST
        @Path("e3_excludes")
        public DataResponse<E3> create_excludes(@Context UriInfo uriInfo, String requestBody) {

            AgRequest agRequest = AgJaxrs.request(config).addExclude("id").build();

            return AgJaxrs.create(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .request(agRequest) // overrides parameters from uriInfo
                    .syncAndSelect(requestBody);
        }

    }

}
