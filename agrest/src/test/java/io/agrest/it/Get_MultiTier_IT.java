package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class Get_MultiTier_IT  extends JerseyTestOnDerby {


    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void test_ToMany_CayenneExp() {

        DB.insert("e5", "id, name", "347, 'C'");
        DB.insert("e5", "id, name", "345, 'B'");
        DB.insert("e5", "id, name", "349, 'E'");
        DB.insert("e5", "id, name", "346, 'A'");
        DB.insert("e5", "id, name", "348, 'D'");

        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e2", "id, name", "2, 'yyy'");

        DB.insert("e3", "id, e2_id, e5_id, name", "8, 1, 345, 'a'");
        DB.insert("e3", "id, e2_id, e5_id, name", "9, 1, 345, 'z'");
        DB.insert("e3", "id, e2_id, e5_id, name", "7, 1, 346, 'm'");
        DB.insert("e3", "id, e2_id, e5_id, name", "5, 1, 348, 'f'");
        DB.insert("e3", "id, e2_id, e5_id, name", "6, 2, 347, 'g'");

        Response r = target("/e2")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"id not in ($id)\",\"params\":{\"id\":2}}"))
                .queryParam("include", urlEnc("[\"id\", {\"path\":\"e3s\", \"include\":[\"e5.name\"]}]"))
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"cayenneExp\":{\"exp\":\"e5 != $id\", \"params\":{\"id\":348}}}"))
                .queryParam("include", urlEnc("{\"path\":\"e3s.e5\",\"cayenneExp\":{\"exp\":\"name not in ($n)\",\"params\":{\"n\":\"E\"}}}"))
                .queryParam("exclude", urlEnc("[{\"e3s\": [\"id\", \"phoneNumber\"]}, {\"e3s.e5\": [\"id\"]}]"))
                .request().get();

        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":[{\"e5\":{\"name\":\"B\"},\"name\":\"a\"},"
                + "{\"e5\":{\"name\":\"B\"},\"name\":\"z\"},"
                + "{\"e5\":{\"name\":\"A\"},\"name\":\"m\"}]}],\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void test_ToMany_CayenneExpById() {

        DB.insert("e5", "id, name", "545, 'B'");
        DB.insert("e5", "id, name", "546, 'A'");
        DB.insert("e2", "id, name", "51, 'xxx'");
        DB.insert("e3", "id, e2_id, e5_id, name", "58, 51, 545, 's'");
        DB.insert("e3", "id, e2_id, e5_id, name", "59, 51, 545, 'z'");
        DB.insert("e3", "id, e2_id, e5_id, name", "57, 51, 546, 'b'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"cayenneExp\":{\"exp\":\"e5 = $id\", \"params\":{\"id\":546}}}"))
                .queryParam("include", "id")
                .request()
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":51,\"e3s\":["
                + "{\"id\":57,\"name\":\"b\",\"phoneNumber\":null}]}],\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void test_parentWithoutChild_CayenneExpByName() {

        DB.insert("e5", "id, name", "545, 'B'");
        DB.insert("e5", "id, name", "546, 'A'");
        DB.insert("e5", "id, name", "547, 'C'");
        DB.insert("e2", "id, name", "51, 'xxx'");
        DB.insert("e2", "id, name", "52, 'yyy'");
        DB.insert("e3", "id, e2_id, e5_id, name", "58, 51, 545, 's'");
        DB.insert("e3", "id, e2_id, e5_id, name", "59, 51, 545, 'z'");
        DB.insert("e3", "id, e2_id, e5_id, name", "57, 51, 546, 'b'");
        DB.insert("e3", "id, e2_id, name", "60, 51, 'z'");

        Response r = target("/e2")
                .queryParam("include", "id")
                .queryParam("include", "name")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"cayenneExp\":{\"exp\":\"name = 'z'\"}}"))
                .request()
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":51,\"e3s\":["
                + "{\"id\":59,\"name\":\"z\",\"phoneNumber\":null},{\"id\":60,\"name\":\"z\",\"phoneNumber\":null}],\"name\":\"xxx\"},{\"id\":52,\"e3s\":[],\"name\":\"yyy\"}],\"total\":2}", r.readEntity(String.class));
    }



    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E2.class).uri(uriInfo).get();
        }

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E3.class).uri(uriInfo).get();
        }
    }
}
