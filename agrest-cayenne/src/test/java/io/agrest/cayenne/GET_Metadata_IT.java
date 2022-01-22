package io.agrest.cayenne;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.MetadataResponse;
import io.agrest.SimpleResponse;
import io.agrest.annotation.AgResource;
import io.agrest.annotation.LinkType;
import io.agrest.cayenne.cayenne.main.E19;
import io.agrest.cayenne.cayenne.main.E5;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.runtime.AgBuilder;
import io.agrest.runtime.jackson.JacksonService;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import static org.junit.jupiter.api.Assertions.*;

@Deprecated
public class GET_Metadata_IT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(E5Resource.class, E19Resource.class)

            .agCustomizer(GET_Metadata_IT::customize)
            .build();

    private static AgBuilder customize(AgBuilder builder) {
        return builder.baseUrl("https://example.org");
    }

    @Test
    public void testGetMetadataForResource() {

        tester.target("/e5/metadata")
                .get()
                .wasOk()
                .bodyEquals("{\"entity\":{\"name\":\"E5\"," +
                        "\"properties\":[" +
                        "{\"name\":\"date\",\"type\":\"date\",\"format\":\"date-time\"}," +
                        "{\"name\":\"e15s\",\"type\":\"E15\",\"relationship\":true,\"collection\":true}," +
                        "{\"name\":\"e3s\",\"type\":\"E3\",\"relationship\":true,\"collection\":true}," +
                        "{\"name\":\"name\",\"type\":\"string\"}]}," +
                        "\"links\":[" +
                        "{\"href\":\"https://example.org/e5\",\"type\":\"collection\",\"operations\":[{\"method\":\"GET\"}]}," +
                        "{\"href\":\"https://example.org/e5/metadata\",\"type\":\"metadata\",\"operations\":[{\"method\":\"GET\"}]}," +
                        "{\"href\":\"https://example.org/e5/{id}\",\"type\":\"item\"," +
                        "\"operations\":[{\"method\":\"DELETE\"},{\"method\":\"GET\"}]}]}");
    }

    @Test
    public void testMetadata_PropertyTypes() {

        String body = tester.target("/e19/metadata").get().wasOk().getContentAsString();

        JsonNode jsonNode = new JacksonService().parseJson(body);
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

    @Path("e5")
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
            return Ag.metadata(E5.class, config).forResource(E5Resource.class).baseUri(uriInfo.getBaseUri()).process();
        }
    }

    @Path("e19")
    public static class E19Resource {

        @Context
        private Configuration config;

        @GET
        @Path("metadata")
        @AgResource(entityClass = E19.class, type = LinkType.METADATA)
        public MetadataResponse<E19> getMetadata(@Context UriInfo uriInfo) {
            return Ag.metadata(E19.class, config)
                    .forResource(E19Resource.class)
                    .baseUri(uriInfo.getBaseUri())
                    .process();
        }
    }

}
