package io.agrest.cayenne;

import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Include;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.cayenne.cayenne.main.E3;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;


public class PUT_AgRequestIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E3.class)
            .build();

    @Test
    public void testPUT_Includes_OverrideByAgRequest() {

        tester.e3().insertColumns("id_", "name")
                .values(5, "aaa")
                .values(4, "zzz")
                .values(2, "bbb")
                .values(6, "yyy").exec();

        String entity = "[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]";

        tester.target("/e3_includes")
                .queryParam("include", "id")
                .put(entity)
                .wasOk()
                // returns names instead of id's due to overriding include by AgRequest
                .bodyEquals(4,
                        "{\"name\":\"yyy\"}",
                        "{\"name\":\"zzz\"}",
                        "{\"name\":\"111\"}",
                        "{\"name\":\"333\"}");
    }

    @Test
    public void testPUT_Excludes_OverrideByAgRequest() {

        tester.e3().insertColumns("id_", "name")
                .values(5, "aaa")
                .values(4, "zzz")
                .values(2, "bbb")
                .values(6, "yyy").exec();

        String entity = "[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]";

        tester.target("/e3_excludes")
                .queryParam("exclude", E3.NAME.getName())
                .put(entity)
                .wasOk()
                // returns 'name' and 'phoneNumber' fields except 'id' due to overriding exclude by AgRequest
                .bodyEquals(4,
                        "{\"name\":\"yyy\",\"phoneNumber\":null}",
                        "{\"name\":\"zzz\",\"phoneNumber\":null}",
                        "{\"name\":\"111\",\"phoneNumber\":null}",
                        "{\"name\":\"333\",\"phoneNumber\":null}");
    }


    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


        @PUT
        @Path("e3_includes")
        public DataResponse<E3> syncE3_includes(@Context UriInfo uriInfo, String requestBody) {
            AgRequest agRequest = AgJaxrs.request(config).addInclude(new Include("name")).build();

            return AgJaxrs.idempotentFullSync(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .request(agRequest) // overrides parameters from uriInfo
                    .syncAndSelect(requestBody);
        }

        @PUT
        @Path("e3_excludes")
        public DataResponse<E3> syncE3_excludes(@Context UriInfo uriInfo, String requestBody) {
            AgRequest agRequest = AgJaxrs.request(config).addExclude(new Exclude("id")).build();

            return AgJaxrs.idempotentFullSync(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .request(agRequest) // overrides parameters from uriInfo
                    .syncAndSelect(requestBody);
        }

    }
}
