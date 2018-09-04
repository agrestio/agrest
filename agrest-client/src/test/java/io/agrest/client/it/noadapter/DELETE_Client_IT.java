package io.agrest.client.it.noadapter;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.DataResponse;
import io.agrest.AgREST;
import io.agrest.SimpleResponse;
import io.agrest.client.ClientDataResponse;
import io.agrest.client.ClientSimpleResponse;
import io.agrest.client.AgRESTClient;
import io.agrest.client.AgRESTClientException;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;

public class DELETE_Client_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
    }

    @Test
    public void testClient_Delete() {

        // Create new entity
        ClientDataResponse<JsonNode> r1 = AgRESTClient.client(target("/e2"))
                .exclude(E2.ADDRESS.getName(), E2.E3S.getName())
                .post(JsonNode.class, "{\"name\":\"xxx\"}");

        assertEquals(Status.CREATED, r1.getStatus());
        assertEquals(1, r1.getTotal());

        int id = r1.getData().get(0).get(E2.ID_PK_COLUMN).asInt();
        JsonNode e2 = EntityUtil.createE2(id, "xxx");
        assertEquals(e2, r1.getData().get(0));

        // Delete existing entity
        ClientSimpleResponse r2 = AgRESTClient.client(target("/e2/" + id))
                .exclude(E2.ADDRESS.getName(), E2.E3S.getName())
                .delete();

        assertEquals(Status.OK, r2.getStatus());

        // Try to fetch deleted entity
        AgRESTClientException e = null;
        try {
            AgRESTClient.client(target("/e2/" + id))
                    .exclude(E2.ADDRESS.getName(), E2.E3S.getName())
                    .get(JsonNode.class);
        } catch (AgRESTClientException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertTrue(e.getMessage().startsWith("Server returned 404 (Not Found)"));
    }

    @Path("e2")
    public static class E2Resource {

        @Context
        private Configuration config;

        @GET
        @Path("{id}")
        public DataResponse<E2> getE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgREST.service(config).selectById(E2.class, id, uriInfo);
        }

        @POST
        public DataResponse<E2> createE2(String targetData, @Context UriInfo uriInfo) {
            return AgREST.create(E2.class, config).uri(uriInfo).syncAndSelect(targetData);
        }

        @DELETE
        @Path("{id}")
        public SimpleResponse deleteE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgREST.service(config).delete(E2.class, id);
        }
    }
}
