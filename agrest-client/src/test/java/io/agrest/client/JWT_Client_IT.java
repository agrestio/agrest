package io.agrest.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.client.unit.ClientDbTest;
import io.agrest.runtime.AgBuilder;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.annotation.Priority;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JWT_Client_IT extends ClientDbTest {

    final static String AUTH_TOKEN = "itIsMyVerySecretToken";

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .agCustomizer(JWT_Client_IT::registerFilter)
            .entities(E4.class)
            .build();


    private static AgBuilder registerFilter(AgBuilder agBuilder) {
        Feature f = c -> {
            c.register(JWTAuthFilter.class);
            return true;
        };

        return agBuilder.feature(f);
    }

    @Test
    public void testAuthClient() {

        tester.e4().insertColumns("id", "c_varchar", "c_int")
                .values(1, "xxx", 5)
                .values(2, "yyy", 7)
                .exec();

        ClientDataResponse<JsonNode> response = client(tester, "/e4")
                .configure(b -> b.header(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_TOKEN))
                .get(JsonNode.class);

        assertEquals(Response.Status.OK, response.getStatus());
        assertEquals(2, response.getTotal());

        List<JsonNode> items = response.getData();
        assertNotNull(items);
        assertEquals(2, items.size());
    }

    @Test
    public void testNotAuthClient() {

        tester.e4().insertColumns("id", "c_varchar", "c_int")
                .values(1, "xxx", 5)
                .values(2, "yyy", 7)
                .exec();

        AgClientException e = null;
        try {
            client(tester, "/e4").get(JsonNode.class);
        } catch (AgClientException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertTrue(e.getMessage().startsWith("Server returned 401 (Unauthorized)"));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E4.class).uri(uriInfo).get();
        }
    }

    @Priority(Priorities.AUTHENTICATION)
    public static class JWTAuthFilter implements ContainerRequestFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) {

            String authHeaderVal = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

            if (authHeaderVal == null
                    || !authHeaderVal.startsWith("Bearer")
                    || !authHeaderVal.contains(AUTH_TOKEN)) {
                System.out.println("No JWT token !");
                requestContext.setProperty("auth-failed", true);
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        }
    }
}
