package com.nhl.link.rest.it.noadapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.resource.E19Resource;
import com.nhl.link.rest.it.fixture.resource.E5Resource;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;

public class GET_Metadata_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E5Resource.class);
        context.register(E19Resource.class);
    }

    @Test
    public void testGetMetadataForResource() {
        WebTarget target = target("/e5/metadata");
        URI uri = target.getUri();
        String baseUrl = uri.toString().replace(uri.getPath(), "");

        Response response1 = target.request().get();
		assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());

        assertEquals(
                "{" +
                        "\"entity\":{" +
                            "\"name\":\"E5\"," +
                            "\"properties\":[" +
                                "{\"name\":\"date\",\"type\":\"date\"}," +
                                "{\"name\":\"e2s\",\"type\":\"E3\",\"relationship\":true,\"collection\":true}," +
                                "{\"name\":\"name\",\"type\":\"string\"}]}," +
                            "\"links\":[" +
                            "{\"href\":\"" + baseUrl + "/e5\",\"type\":\"collection\"," +
                               "\"operations\":[{\"method\":\"GET\"}]}," +
                            "{\"href\":\"" + baseUrl + "/e5/metadata\",\"type\":\"metadata\"," +
                               "\"operations\":[{\"method\":\"GET\"}]}," +
                            "{\"href\":\"" + baseUrl + "/e5/{id}\",\"type\":\"item\"," +
                                "\"operations\":[{\"method\":\"GET\"},{\"method\":\"DELETE\"}]}]}"
                            ,
                response1.readEntity(String.class)
        );
    }

    @Test
	public void testMetadata_PropertyTypes() throws IOException {

		WebTarget target = target("/e19/metadata");

        Response response1 = target.request().get();
		assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());

        JsonNode jsonNode = new JacksonService().parseJson(response1.readEntity(String.class));
        assertEquals("[{\"name\":\"bigDecimal\",\"type\":\"number\"},{\"name\":\"bigInteger\",\"type\":\"number\"}," +
                "{\"name\":\"booleanObject\",\"type\":\"boolean\"},{\"name\":\"booleanPrimitive\",\"type\":\"boolean\"}," +
                "{\"name\":\"byteObject\",\"type\":\"number\"},{\"name\":\"bytePrimitive\",\"type\":\"number\"}," +
                "{\"name\":\"cDate\",\"type\":\"date\"},{\"name\":\"cString\",\"type\":\"string\"}," +
                "{\"name\":\"cTime\",\"type\":\"date\"},{\"name\":\"cTimestamp\",\"type\":\"date\"}," +
                "{\"name\":\"charObject\",\"type\":\"string\"},{\"name\":\"charPrimitive\",\"type\":\"string\"}," +
                "{\"name\":\"doubleObject\",\"type\":\"number\"},{\"name\":\"doublePrimitive\",\"type\":\"number\"}," +
                "{\"name\":\"floatObject\",\"type\":\"number\"},{\"name\":\"floatPrimitive\",\"type\":\"number\"}," +
                "{\"name\":\"guid\",\"type\":\"string\"},{\"name\":\"intObject\",\"type\":\"number\"}," +
                "{\"name\":\"intPrimitive\",\"type\":\"number\"},{\"name\":\"longObject\",\"type\":\"number\"}," +
                "{\"name\":\"longPrimitive\",\"type\":\"number\"},{\"name\":\"shortObject\",\"type\":\"number\"}," +
                "{\"name\":\"shortPrimitive\",\"type\":\"number\"}]", jsonNode.get("entity").get("properties").toString());
	}

}
