package io.agrest.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.client.protocol.Include;
import io.agrest.client.unit.ClientDbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static io.agrest.client.unit.EntityUtil.createE2;
import static io.agrest.client.unit.EntityUtil.createE3;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class POST_Client_IT extends ClientDbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @Test
    public void testClient_Post() {

        // Create related entity
        ClientDataResponse<JsonNode> r1 = client(tester, "/e3")
                .exclude("phoneNumber")
                .post(JsonNode.class, "{\"name\":\"ccc\"}");

        assertEquals(Status.CREATED, r1.getStatus());
        assertEquals(1, r1.getTotal());

        int e3_id = r1.getData().get(0).get("id").asInt();
        JsonNode e3 = createE3(e3_id, "ccc");
        assertEquals(e3, r1.getData().get(0));

        // Create parent entity
        ClientDataResponse<JsonNode> r2 = client(tester, "/e2")
                .exclude("address", "e3s.phoneNumber")
                .include(Include.path("e3s"))
                .post(JsonNode.class, "{\"name\":\"xxx\",\"address\":\"yyy\",\"e3s\":[" + e3_id + "]}");

        assertEquals(Status.CREATED, r2.getStatus());
        assertEquals(1, r2.getTotal());

        int e2_id = r2.getData().get(0).get("id").asInt();
        assertEquals(createE2(e2_id, "xxx", e3), r2.getData().get(0));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e2")
        public DataResponse<E2> createE2(String targetData, @Context UriInfo uriInfo) {
            return Ag.create(E2.class, config).uri(uriInfo).syncAndSelect(targetData);
        }

        @POST
        @Path("e3")
        public DataResponse<E3> create(@Context UriInfo uriInfo, String requestBody) {
            return Ag.create(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
        }
    }
}
