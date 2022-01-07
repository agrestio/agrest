package io.agrest.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.client.unit.ClientDbTest;
import io.agrest.client.unit.EntityUtil;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.*;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static org.junit.jupiter.api.Assertions.*;

public class DELETE_Client_IT extends ClientDbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @Test
    public void testClient_Delete() {

        // Create new entity
        ClientDataResponse<JsonNode> r1 = client(tester, "/e2")
                .exclude(E2.ADDRESS.getName(), E2.E3S.getName())
                .post(JsonNode.class, "{\"name\":\"xxx\"}");

        assertEquals(Status.CREATED, r1.getStatus());
        assertEquals(1, r1.getTotal());

        int id = r1.getData().get(0).get("id").asInt();
        JsonNode e2 = EntityUtil.createE2(id, "xxx");
        assertEquals(e2, r1.getData().get(0));

        // Delete existing entity
        ClientSimpleResponse r2 = client(tester, "/e2/" + id)
                .exclude(E2.ADDRESS.getName(), E2.E3S.getName())
                .delete();

        assertEquals(Status.OK, r2.getStatus());

        // Try to fetch deleted entity
        AgClientException e = null;
        try {
            client(tester, "/e2/" + id)
                    .exclude(E2.ADDRESS.getName(), E2.E3S.getName())
                    .get(JsonNode.class);
        } catch (AgClientException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertTrue(e.getMessage().startsWith("Server returned 404 (Not Found)"));
    }

    @Path("e2")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("{id}")
        public DataResponse<E2> getE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.select(E2.class, config).uri(uriInfo).byId(id).get();
        }

        @POST
        public DataResponse<E2> createE2(String targetData, @Context UriInfo uriInfo) {
            return Ag.create(E2.class, config).uri(uriInfo).syncAndSelect(targetData);
        }

        @DELETE
        @Path("{id}")
        public SimpleResponse deleteE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.delete(E2.class, config).byId(id).sync();
        }
    }
}
