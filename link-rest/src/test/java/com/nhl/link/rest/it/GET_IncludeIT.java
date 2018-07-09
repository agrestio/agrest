package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.cayenne.E5;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertEquals;

public class GET_IncludeIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void test_PathAttribute() {

        SQLTemplate insert = new SQLTemplate(E4.class, "INSERT INTO utest.e4 (c_int) values (55)");
        newContext().performGenericQuery(insert);

        Response response1 = target("/e4").queryParam("include", urlEnc("{\"path\":\"cInt\"}")).request().get();
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response1.getStatus());
        assertEquals(
                "{\"success\":false,\"message\":\"Bad include spec, non-relationship 'path' in include object: cInt\"}",
                response1.readEntity(String.class));
    }

    @Test
    public void test_PathRelationship() {

        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));

        Response response1 = target("/e3").queryParam("include", urlEnc("{\"path\":\"e2\"}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"}}],\"total\":1}",
                response1.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToOne() {

        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));

        Response response1 = target("/e3").queryParam("include", urlEnc("{\"path\":\"e2\",\"mapBy\":\"name\"}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());

        // no support for MapBy for to-one... simply ignoring it...
        assertEquals("{\"data\":[{\"id\":8,\"e2\":{"
                + "\"id\":1,\"address\":null,\"name\":\"xxx\"}}],\"total\":1}", response1.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToMany() {

        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'aaa')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (7, 1, 'aaa')"));

        Response response1 = target("/e2").queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"name\"}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"e3s\":{"
                        + "\"aaa\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}],"
                        + "\"zzz\":[{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}]" + "}}],\"total\":1}",
                response1.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToMany_ById() {

        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'aaa')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (7, 1, 'aaa')"));

        Response response1 = target("/e2").queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"id\"}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":{"
                        + "\"8\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null}],"
                        + "\"9\":[{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}],"
                        + "\"7\":[{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}]" + "}}],\"total\":1}",
                response1.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToMany_ByRelatedId() {

        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e5 (id) values (45),(46)"));
        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, e5_id, name) "
                        + "values (8, 1, 45, 'aaa'),(9, 1, 45, 'zzz'),(7, 1, 46, 'aaa')"));

        Response response1 = target("/e2").queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"e5.id\"}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"e3s\":{"
                        + "\"45\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}],"
                        + "\"46\":[{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}]" + "}}],\"total\":1}",
                response1.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToMany_ByRelatedAttribute() {

        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e5 (id,name) values (45, 'T'),(46, 'Y')"));
        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, e5_id, name) "
                        + "values (8, 1, 45, 'aaa'),(9, 1, 45, 'zzz'),(7, 1, 46, 'aaa')"));

        Response response1 = target("/e2").queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"e5.name\"}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"e3s\":{"
                        + "\"T\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}],"
                        + "\"Y\":[{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}]" + "}}],\"total\":1}",
                response1.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToMany_ByRelatedDate() {

        newContext().performGenericQuery(
                new SQLTemplate(E5.class,
                        "INSERT INTO utest.e5 (id,name,date) values (45, 'T','2013-01-03'),(46, 'Y','2013-01-04')"));
        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, e5_id, name) "
                        + "values (8, 1, 45, 'aaa'),(9, 1, 45, 'zzz'),(7, 1, 46, 'aaa')"));

        Response response1 = target("/e2").queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"e5.date\"}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"e3s\":{"
                        + "\"2013-01-03T00:00:00\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}],"
                        + "\"2013-01-04T00:00:00\":[{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}]" + "}}],\"total\":1}",
                response1.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToMany_WithCayenneExp() {

        // see LF-294 - filter applied too late may cause a LinkRestException

        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'aaa')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (7, 1, 'aaa')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (6, 1, NULL)"));

        Response response1 = target("/e2")
                .queryParam("include",
                        urlEnc("{\"path\":\"e3s\",\"mapBy\":\"name\", \"cayenneExp\":{\"exp\":\"name != NULL\"}}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"e3s\":{"
                        + "\"aaa\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}],"
                        + "\"zzz\":[{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}]" + "}}],\"total\":1}",
                response1.readEntity(String.class));
    }

    @Test
    public void test_ToMany_Sort() {

        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values "
                        + "(8, 1, 'z'),(9, 1, 's'),(7, 1, 'b')"));

        Response response1 = target("/e2").queryParam("include", urlEnc("{\"path\":\"e3s\",\"sort\":\"name\"}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":["
                + "{\"id\":7,\"name\":\"b\",\"phoneNumber\":null}," + "{\"id\":9,\"name\":\"s\",\"phoneNumber\":null},"
                + "{\"id\":8,\"name\":\"z\",\"phoneNumber\":null}]}],\"total\":1}", response1.readEntity(String.class));
    }

    @Test
    public void test_ToMany_SortPath() {

        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e5 (id,name) values (145, 'B'),(146, 'A')"));
        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (11, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, e5_id, name) "
                        + "values (18, 11, 145, 's'),(19, 11, 145, 'z'),(17, 11, 146, 'b')"));

        Response response1 = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"sort\":[{\"property\":\"e5.name\"}]}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":11,\"e3s\":["
                + "{\"id\":17,\"name\":\"b\",\"phoneNumber\":null},"
                + "{\"id\":18,\"name\":\"s\",\"phoneNumber\":null},"
                + "{\"id\":19,\"name\":\"z\",\"phoneNumber\":null}]}],\"total\":1}", response1.readEntity(String.class));
    }

    @Test
    public void test_ToMany_SortPath_Dir() {

        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e5 (id,name) values (245, 'B'),(246, 'A')"));
        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (21, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, e5_id, name) "
                        + "values (28, 21, 245, 's'),(29, 21, 245, 'z'),(27, 21, 246, 'b')"));

        Response response1 = target("/e2")
                .queryParam(
                        "include",
                        urlEnc("{\"path\":\"e3s\",\"sort\":[{\"property\":\"e5.name\", \"direction\":\"DESC\"},{\"property\":\"name\", \"direction\":\"DESC\"}]}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":21,\"e3s\":["
                + "{\"id\":29,\"name\":\"z\",\"phoneNumber\":null},"
                + "{\"id\":28,\"name\":\"s\",\"phoneNumber\":null},"
                + "{\"id\":27,\"name\":\"b\",\"phoneNumber\":null}]}],\"total\":1}", response1.readEntity(String.class));
    }

    @Test
    public void test_ToMany_CayenneExp() {

        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) "
                        + "values (8, 1, 'a'),(9, 1, 'z'),(7, 1, 'a')"));

        Response response1 = target("/e2")
                .queryParam("include",
                        urlEnc("{\"path\":\"e3s\",\"cayenneExp\":{\"exp\":\"name = $n\", \"params\":{\"n\":\"a\"}}}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":["
                + "{\"id\":8,\"name\":\"a\",\"phoneNumber\":null},"
                + "{\"id\":7,\"name\":\"a\",\"phoneNumber\":null}]}],\"total\":1}", response1.readEntity(String.class));
    }

    @Test
    public void test_ToMany_CayenneExpById() {

        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e5 (id,name) values (545, 'B'),(546, 'A')"));
        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (51, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, e5_id, name) "
                        + "values (58, 51, 545, 's'),(59, 51, 545, 'z'),(57, 51, 546, 'b')"));

        Response response1 = target("/e2")
                .queryParam("include",
                        urlEnc("{\"path\":\"e3s\",\"cayenneExp\":{\"exp\":\"e5 = $id\", \"params\":{\"id\":546}}}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":51,\"e3s\":["
                + "{\"id\":57,\"name\":\"b\",\"phoneNumber\":null}]}],\"total\":1}", response1.readEntity(String.class));
    }

    @Test
    public void test_ToMany_Exclude() {

        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) "
                        + "values (8, 1, 'a'),(9, 1, 'z'),(7, 1, 'm')"));

        Response response1 = target("/e2").queryParam("include", urlEnc("{\"path\":\"e3s\"}"))
                .queryParam("include", "id").queryParam("exclude", "e3s.id").queryParam("exclude", "e3s.phoneNumber")
                .request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":[{\"name\":\"a\"}," + "{\"name\":\"z\"},"
                + "{\"name\":\"m\"}]}],\"total\":1}", response1.readEntity(String.class));
    }

    @Test
    public void test_ToMany_IncludeRelated() {
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e5 (id,name) values (345, 'B'),(346, 'A')"));
        newContext().performGenericQuery(
                new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, e5_id, name) "
                        + "values (8, 1, 345, 'a'),(9, 1, 345, 'z'),(7, 1, 346, 'm')"));

        Response response1 = target("/e2").queryParam("include", urlEnc("{\"path\":\"e3s\"}"))
                .queryParam("include", "id").queryParam("exclude", "e3s.id").queryParam("exclude", "e3s.phoneNumber")
                .queryParam("include", "e3s.e5.name").request().get();

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":[{\"e5\":{\"name\":\"B\"},\"name\":\"a\"},"
                + "{\"e5\":{\"name\":\"B\"},\"name\":\"z\"},"
                + "{\"e5\":{\"name\":\"A\"},\"name\":\"m\"}]}],\"total\":1}", response1.readEntity(String.class));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            return LinkRest.service(config).select(E2.class).uri(uriInfo).get();
        }

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return LinkRest.service(config).select(E3.class).uri(uriInfo).get();
        }

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return LinkRest.service(config).select(E4.class).uri(uriInfo).get();
        }
    }
}
