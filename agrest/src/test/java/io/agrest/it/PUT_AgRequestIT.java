package io.agrest.it;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Include;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


public class PUT_AgRequestIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testPUT_Includes_OverrideByAgRequest() {

        insert("e3", "id, name", "5, 'aaa'");
        insert("e3", "id, name", "4, 'zzz'");
        insert("e3", "id, name", "2, 'bbb'");
        insert("e3", "id, name", "6, 'yyy'");

        Entity<String> entity = Entity.json(
                "[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]");
        Response response = target("/e3_includes")
                .queryParam("include", "id")
                .request()
                .put(entity);

        // returns names instead of id's due to overriding include by AgRequest
        onSuccess(response).bodyEquals(4,
                "{\"name\":\"yyy\"}",
                "{\"name\":\"zzz\"}",
                "{\"name\":\"111\"}",
                "{\"name\":\"333\"}");
    }

    @Test
    public void testPUT_Excludes_OverrideByAgRequest() {

        insert("e3", "id, name", "5, 'aaa'");
        insert("e3", "id, name", "4, 'zzz'");
        insert("e3", "id, name", "2, 'bbb'");
        insert("e3", "id, name", "6, 'yyy'");

        Entity<String> entity = Entity.json(
                "[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]");
        Response response = target("/e3_excludes")
                .queryParam("exclude", E3.NAME.getName())
                .request()
                .put(entity);

        // returns 'name' and 'phoneNumber' fields except 'id' due to overriding exclude by AgRequest
        onSuccess(response).bodyEquals(4,
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
            AgRequest agRequest = Ag.request(config).addInclude(new Include("name")).build();

            return Ag.idempotentFullSync(E3.class, config)
                    .uri(uriInfo)
                    .request(agRequest) // overrides parameters from uriInfo
                    .syncAndSelect(requestBody);
        }

        @PUT
        @Path("e3_excludes")
        public DataResponse<E3> syncE3_excludes(@Context UriInfo uriInfo, String requestBody) {
            AgRequest agRequest = Ag.request(config).addExclude(new Exclude("id")).build();

            return Ag.idempotentFullSync(E3.class, config)
                    .uri(uriInfo)
                    .request(agRequest) // overrides parameters from uriInfo
                    .syncAndSelect(requestBody);
        }

    }
}
