package io.agrest.client.it.noadapter;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.client.AgClient;
import io.agrest.client.AgClientException;
import io.agrest.client.ClientDataResponse;
import io.agrest.it.fixture.BQJerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E4;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Priority;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class JWT_Client_IT extends BQJerseyTestOnDerby {

    final static String AUTH_TOKEN = "itIsMyVerySecretToken";

    @BeforeClass
    public static void startTestRuntime() {
        Feature f = c -> {
            c.register(JWTAuthFilter.class);
            return true;
        };
        startTestRuntime(ab -> ab.feature(f), Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E4.class};
    }

    @Test
    public void testAuthClient() {

        e4().insertColumns("id", "c_varchar", "c_int")
                .values(1, "xxx", 5)
                .values(2, "yyy", 7)
                .exec();

        ClientDataResponse<JsonNode> response = AgClient.client(target("/e4"))
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

        e4().insertColumns("id", "c_varchar", "c_int")
                .values(1, "xxx", 5)
                .values(2, "yyy", 7)
                .exec();

        AgClientException e = null;
        try {
            AgClient.client(target("/e4")).get(JsonNode.class);
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
        public void filter(ContainerRequestContext requestContext) throws IOException {

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
