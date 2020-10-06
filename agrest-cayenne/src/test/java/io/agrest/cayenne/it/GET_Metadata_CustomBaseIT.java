package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.MetadataResponse;
import io.agrest.annotation.AgResource;
import io.agrest.annotation.LinkType;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E5;
import io.agrest.runtime.AgBuilder;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GET_Metadata_CustomBaseIT extends JerseyAndDerbyCase {

    @BQTestTool
    static final CayenneAgTester tester = tester(Resource.class)

            .agCustomizer(GET_Metadata_CustomBaseIT::customize)
            .build();

    private static AgBuilder customize(AgBuilder builder) {
        return builder.baseUrl("https://example.org");
    }

    @Test
    public void testGetMetadataForResource() {

        String json = tester.target("/r1/meta")
                .get()
                .wasSuccess()
                .getContentAsString();

        assertTrue(json.contains("{\"href\":\"https://example.org/r1/meta\",\"type\":\"metadata\",\"operations\":[{\"method\":\"GET\"}]}"));
    }

    @Path("r1")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("meta")
        @AgResource(entityClass = E5.class, type = LinkType.METADATA)
        public MetadataResponse<E5> getMetadata(@Context UriInfo uriInfo) {
            return Ag.metadata(E5.class, config).forResource(Resource.class).uri(uriInfo).process();
        }
    }

}
