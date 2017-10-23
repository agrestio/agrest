package com.nhl.link.rest.client.it.noadapter;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.client.ClientDataResponse;
import com.nhl.link.rest.client.LinkRestClient;
import com.nhl.link.rest.it.fixture.pojo.JerseyTestOnPojo;
import com.nhl.link.rest.it.fixture.pojo.model.P1;
import com.nhl.link.rest.it.fixture.pojo.model.P2;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GET_Client_PojoIT extends JerseyTestOnPojo {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testClient() {
        P2 expected = new P2();
        expected.setName("abc");
        P1 related = new P1();
        related.setName("xyz");
        expected.setP1(related);
        pojoDB.bucketForType(P2.class).put(1, expected);

        ClientDataResponse<P2> response = LinkRestClient.client(target("/p2")).include("p1").get(P2.class);
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
            return LinkRest.service(config).select(P2.class).uri(uriInfo).get();
        }
    }
}
