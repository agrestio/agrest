package com.nhl.link.rest.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.annotation.LinkType;
import com.nhl.link.rest.annotation.LrResource;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E19;
import com.nhl.link.rest.it.fixture.resource.E5Resource;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;

public class GET_Metadata_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E5Resource.class);
        context.register(Resource.class);
    }

    @Test
    public void testGetMetadataForResource() {
        WebTarget target = target("/e5/metadata");

        Response response1 = target.request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());

        assertEquals(
                "{\"entity\":{\"name\":\"E5\"," +
                        "\"properties\":[" +
                        "{\"name\":\"date\",\"type\":\"date\",\"format\":\"date-time\"}," +
                        "{\"name\":\"e15s\",\"type\":\"E15\",\"relationship\":true,\"collection\":true}," +
                        "{\"name\":\"e2s\",\"type\":\"E3\",\"relationship\":true,\"collection\":true}," +
                        "{\"name\":\"name\",\"type\":\"string\"}]}," +
                        "\"links\":[" +
                        "{\"href\":\"http://localhost:9998/e5\",\"type\":\"collection\",\"operations\":[{\"method\":\"GET\"}]}," +
                        "{\"href\":\"http://localhost:9998/e5/metadata\",\"type\":\"metadata\",\"operations\":[{\"method\":\"GET\"}]}," +
                        "{\"href\":\"http://localhost:9998/e5/metadata-constraints\",\"type\":\"metadata\",\"operations\":[{\"method\":\"GET\"}]}," +
                        "{\"href\":\"http://localhost:9998/e5/{id}\",\"type\":\"item\"," +
                        "\"operations\":[{\"method\":\"GET\"},{\"method\":\"DELETE\"}]}]}"
                ,
                response1.readEntity(String.class)
        );
    }

    @Test
    public void testMetadata_PropertyTypes() {

        WebTarget target = target("/metadata");

        Response r = target.request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());

        JsonNode jsonNode = new JacksonService().parseJson(r.readEntity(String.class));
        assertEquals("[{\"name\":\"bigDecimal\",\"type\":\"number\"}," +
                        "{\"name\":\"bigInteger\",\"type\":\"number\"}," +
                        "{\"name\":\"booleanObject\",\"type\":\"boolean\"}," +
                        "{\"name\":\"booleanPrimitive\",\"type\":\"boolean\"}," +
                        "{\"name\":\"byteObject\",\"type\":\"number\",\"format\":\"int32\"}," +
                        "{\"name\":\"bytePrimitive\",\"type\":\"number\",\"format\":\"int32\"}," +
                        "{\"name\":\"cDate\",\"type\":\"date\",\"format\":\"date-time\"}," +
                        "{\"name\":\"cString\",\"type\":\"string\"}," +
                        "{\"name\":\"cTime\",\"type\":\"date\",\"format\":\"full-time\"}," +
                        "{\"name\":\"cTimestamp\",\"type\":\"date\",\"format\":\"date-time\"}," +
                        "{\"name\":\"charObject\",\"type\":\"string\"}," +
                        "{\"name\":\"charPrimitive\",\"type\":\"string\"}," +
                        "{\"name\":\"doubleObject\",\"type\":\"number\",\"format\":\"double\"}," +
                        "{\"name\":\"doublePrimitive\",\"type\":\"number\",\"format\":\"double\"}," +
                        "{\"name\":\"floatObject\",\"type\":\"number\",\"format\":\"float\"}," +
                        "{\"name\":\"floatPrimitive\",\"type\":\"number\",\"format\":\"float\"}," +
                        "{\"name\":\"guid\",\"type\":\"string\",\"format\":\"byte\"}," +
                        "{\"name\":\"intObject\",\"type\":\"number\",\"format\":\"int32\"}," +
                        "{\"name\":\"intPrimitive\",\"type\":\"number\",\"format\":\"int32\"}," +
                        "{\"name\":\"longObject\",\"type\":\"number\",\"format\":\"int64\"}," +
                        "{\"name\":\"longPrimitive\",\"type\":\"number\",\"format\":\"int64\"}," +
                        "{\"name\":\"shortObject\",\"type\":\"number\",\"format\":\"int32\"}," +
                        "{\"name\":\"shortPrimitive\",\"type\":\"number\",\"format\":\"int32\"}]",
                jsonNode.get("entity").get("properties").toString());
    }

    @Test
    public void testGetMetadata_Constraints() {

        Response r = target("/e5/metadata-constraints").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        String metadata = r.readEntity(String.class);
        assertTrue(metadata.contains("{\"name\":\"name\",\"type\":\"string\"}"));
        assertTrue(metadata.contains("{\"name\":\"e15s\",\"type\":\"E15\",\"relationship\":true,\"collection\":true}"));
        assertFalse("Constraint was not applied",
                metadata.contains("{\"name\":\"date\",\"type\":\"date\",\"format\":\"date-time\"}"));
        assertFalse("Constraint was not applied",
                metadata.contains("{\"name\":\"e2s\",\"type\":\"E3\",\"relationship\":true,\"collection\":true}"));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("metadata")
        @LrResource(entityClass = E19.class, type = LinkType.METADATA)
        public MetadataResponse<E19> getMetadata(@Context UriInfo uriInfo) {
            return LinkRest.metadata(E19.class, config)
                    .forResource(Resource.class)
                    .uri(uriInfo)
                    .process();
        }
    }

}
