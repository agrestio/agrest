package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.MetadataResponse;
import io.agrest.SimpleResponse;
import io.agrest.annotation.AgResource;
import io.agrest.annotation.LinkType;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E5;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_Metadata_EmptyPath_IT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(ab -> ab.baseUrl("https://example.org"), E5Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[0];
    }

    @Test
    public void testGetMetadata() {

        Response r = target("/metadata").request().get();

        onSuccess(r).bodyEquals("{\"entity\":{\"name\":\"E5\"," +
                "\"properties\":[" +
                "{\"name\":\"date\",\"type\":\"date\",\"format\":\"date-time\"}," +
                "{\"name\":\"e15s\",\"type\":\"E15\",\"relationship\":true,\"collection\":true}," +
                "{\"name\":\"e3s\",\"type\":\"E3\",\"relationship\":true,\"collection\":true}," +
                "{\"name\":\"name\",\"type\":\"string\"}]}," +
                "\"links\":[" +
                "{\"href\":\"https://example.org/\",\"type\":\"collection\",\"operations\":[{\"method\":\"GET\"}]}," +
                "{\"href\":\"https://example.org/metadata\",\"type\":\"metadata\",\"operations\":[{\"method\":\"GET\"}]}," +
                "{\"href\":\"https://example.org/{id}\",\"type\":\"item\"," +
                "\"operations\":[{\"method\":\"DELETE\"},{\"method\":\"GET\"}]}]}");
    }

    // test case with empty root path
    @Path("")
    public static class E5Resource {

        @Context
        private Configuration config;

        @GET
        @AgResource(type = LinkType.COLLECTION)
        public DataResponse<E5> get(@Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant for the test");
        }

        @GET
        @Path("{id}")
        @AgResource(type = LinkType.ITEM)
        public DataResponse<E5> getById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant for the test");
        }

        @DELETE
        @Path("{id}")
        public SimpleResponse deleteById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant for the test");
        }

        @GET
        @Path("metadata")
        @AgResource(entityClass = E5.class, type = LinkType.METADATA)
        public MetadataResponse<E5> getMetadata(@Context UriInfo uriInfo) {
            return Ag.metadata(E5.class, config).forResource(E5Resource.class).uri(uriInfo).process();
        }
    }
}
