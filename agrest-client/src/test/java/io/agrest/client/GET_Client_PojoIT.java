package io.agrest.client;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.client.unit.ClientPojoTest;
import io.agrest.pojo.model.P1;
import io.agrest.pojo.model.P2;
import io.agrest.unit.AgPojoTester;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GET_Client_PojoIT extends ClientPojoTest {

    @BQTestTool
    static final AgPojoTester tester = tester(Resource.class).build();

    @Test
    public void testClient() {
        P2 expected = new P2();
        expected.setName("abc");
        P1 related = new P1();
        related.setName("xyz");
        expected.setP1(related);
        tester.p2().put(1, expected);

        ClientDataResponse<P2> response = client(tester, "/p2").include("p1").get(P2.class);
        assertEquals(Status.OK, response.getStatus());
        assertEquals(1, response.getTotal());

        List<P2> p2s = response.getData();
        assertEquals(1, p2s.size());

        P2 actual = p2s.iterator().next();
        assertEquals(expected.getName(), actual.getName());
        assertNotNull(actual.getP1());
        assertEquals(expected.getP1().getName(), actual.getP1().getName());
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("p2")
        public DataResponse<P2> getP2(@Context UriInfo uriInfo) {
            return Ag.service(config).select(P2.class).uri(uriInfo).get();
        }
    }
}
