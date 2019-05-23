package io.agrest.client.it.noadapter;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.client.AgClient;
import io.agrest.client.ClientDataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;

public class PUT_Client_IT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E3.class};
    }

    @Test
    public void testClient_Put() {

        // Create new entity
        ClientDataResponse<JsonNode> r1 = AgClient.client(target("/e3"))
                .exclude(E3.PHONE_NUMBER.getName())
                .post(JsonNode.class, "{\"name\":\"ccc\"}");

        assertEquals(Response.Status.CREATED, r1.getStatus());
        assertEquals(1, r1.getTotal());

        int id = r1.getData().get(0).get("id").asInt();
        JsonNode e3_before_update = EntityUtil.createE3(id, "ccc");
        assertEquals(e3_before_update, r1.getData().get(0));

        // Update existing entity
        ClientDataResponse<JsonNode> r2 = AgClient.client(target("/e3"))
                .exclude(E3.PHONE_NUMBER.getName())
                .put(JsonNode.class, "{\"id\":" + id + ",\"name\":\"ddd\"}");

        assertEquals(Response.Status.OK, r2.getStatus());
        assertEquals(1, r2.getTotal());

        JsonNode e3_after_update = EntityUtil.createE3(id, "ddd");
        assertEquals(e3_after_update, r2.getData().get(0));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e3")
        public DataResponse<E3> create(@Context UriInfo uriInfo, String requestBody) {
            return Ag.create(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e3")
        public DataResponse<E3> sync(@Context UriInfo uriInfo, String requestBody) {
            return Ag.idempotentFullSync(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
        }
    }
}
