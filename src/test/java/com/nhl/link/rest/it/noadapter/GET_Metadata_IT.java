package com.nhl.link.rest.it.noadapter;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.resource.E5Resource;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class GET_Metadata_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E5Resource.class);
    }

    @Test
    public void testGetMetadataForResource() {
        WebTarget target = target("/e5/metadata");
        URI uri = target.getUri();
        String baseUrl = uri.toString().replace(uri.getPath(), "");

        Response response1 = target.request().get();
		assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());

        assertEquals(
                "{\"success\":true," +
                        "\"entity\":{" +
                            "\"name\":\"E5\"," +
                            "\"properties\":[" +
                                "{\"name\":\"date\",\"type\":\"date\"}," +
                                "{\"name\":\"e2s\",\"type\":\"E3\",\"relationship\":true,\"collection\":true}," +
                                "{\"name\":\"name\",\"type\":\"string\"}]}," +
                        "\"links\":[" +
                            "{\"href\":\"" + baseUrl + "/e5/{id}\",\"type\":\"item\"," +
                                "\"operations\":[{\"method\":\"GET\"},{\"method\":\"DELETE\"}]}," +
                            "{\"href\":\"" + baseUrl + "/e5/metadata\",\"type\":\"metadata\"," +
                                "\"operations\":[{\"method\":\"GET\"}]}," +
                            "{\"href\":\"" + baseUrl + "/e5\",\"type\":\"collection\"," +
                                "\"operations\":[{\"method\":\"GET\"}]}]}",
                response1.readEntity(String.class)
        );
    }

}
