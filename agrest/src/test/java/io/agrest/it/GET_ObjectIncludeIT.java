package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
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

public class GET_ObjectIncludeIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void test_PathAttribute() {

        DB.insert("e4", "c_int", "55");

        Response response1 = target("/e4").queryParam("include", urlEnc("{\"path\":\"cInt\"}")).request().get();
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response1.getStatus());
        assertEquals(
                "{\"success\":false,\"message\":\"Bad include spec, non-relationship 'path' in include object: cInt\"}",
                response1.readEntity(String.class));
    }

    @Test
    public void test_PathRelationship() {

        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, name", "8, 1, 'yyy'");

        Response r = target("/e3")
                .queryParam("include", urlEnc("{\"path\":\"e2\"}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"}}],\"total\":1}",
                r.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToOne() {

        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, name", "8, 1, 'yyy'");

        Response r = target("/e3")
                .queryParam("include", urlEnc("{\"path\":\"e2\",\"mapBy\":\"name\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());

        // no support for MapBy for to-one... simply ignoring it...
        assertEquals("{\"data\":[{\"id\":8,\"e2\":{"
                + "\"id\":1,\"address\":null,\"name\":\"xxx\"}}],\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToMany() {

        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, name", "8, 1, 'aaa'");
        DB.insert("e3", "id, e2_id, name", "9, 1, 'zzz'");
        DB.insert("e3", "id, e2_id, name", "7, 1, 'aaa'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"name\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"e3s\":{"
                        + "\"aaa\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}],"
                        + "\"zzz\":[{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}]" + "}}],\"total\":1}",
                r.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToMany_ById() {

        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, name", "8, 1, 'aaa'");
        DB.insert("e3", "id, e2_id, name", "9, 1, 'zzz'");
        DB.insert("e3", "id, e2_id, name", "7, 1, 'aaa'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"id\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":{"
                        + "\"8\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null}],"
                        + "\"9\":[{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}],"
                        + "\"7\":[{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}]" + "}}],\"total\":1}",
                r.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToMany_ByRelatedId() {

        DB.insert("e5", "id", "45");
        DB.insert("e5", "id", "46");
        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, e5_id, name", "8, 1, 45, 'aaa'");
        DB.insert("e3", "id, e2_id, e5_id, name", "9, 1, 45, 'zzz'");
        DB.insert("e3", "id, e2_id, e5_id, name", "7, 1, 46, 'aaa'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"e5.id\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"e3s\":{"
                        + "\"45\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}],"
                        + "\"46\":[{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}]" + "}}],\"total\":1}",
                r.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToMany_ByRelatedAttribute() {

        DB.insert("e5", "id, name", "45, 'T'");
        DB.insert("e5", "id, name", "46, 'Y'");
        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, e5_id, name", "8, 1, 45, 'aaa'");
        DB.insert("e3", "id, e2_id, e5_id, name", "9, 1, 45, 'zzz'");
        DB.insert("e3", "id, e2_id, e5_id, name", "7, 1, 46, 'aaa'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"e5.name\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"e3s\":{"
                        + "\"T\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}],"
                        + "\"Y\":[{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}]" + "}}],\"total\":1}",
                r.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToMany_ByRelatedDate() {

        DB.insert("e5", "id, name, date", "45, 'T', '2013-01-03'");
        DB.insert("e5", "id, name, date", "46, 'Y', '2013-01-04'");
        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, e5_id, name", "8, 1, 45, 'aaa'");
        DB.insert("e3", "id, e2_id, e5_id, name", "9, 1, 45, 'zzz'");
        DB.insert("e3", "id, e2_id, e5_id, name", "7, 1, 46, 'aaa'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"e5.date\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"e3s\":{"
                        + "\"2013-01-03T00:00:00\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}],"
                        + "\"2013-01-04T00:00:00\":[{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}]" + "}}],\"total\":1}",
                r.readEntity(String.class));
    }

    @Test
    public void test_MapBy_ToMany_WithCayenneExp() {

        // see LF-294 - filter applied too late may cause a AgException

        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, name", "8, 1, 'aaa'");
        DB.insert("e3", "id, e2_id, name", "9, 1, 'zzz'");
        DB.insert("e3", "id, e2_id, name", "7, 1, 'aaa'");
        DB.insert("e3", "id, e2_id, name", "6, 1, NULL");

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"name\", \"cayenneExp\":{\"exp\":\"name != NULL\"}}"))
                .queryParam("include", "id")
                .request()
                .get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"e3s\":{"
                        + "\"aaa\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}],"
                        + "\"zzz\":[{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}]" + "}}],\"total\":1}",
                r.readEntity(String.class));
    }

    @Test
    public void test_ToMany_Sort() {

        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, name", "8, 1, 'z'");
        DB.insert("e3", "id, e2_id, name", "9, 1, 's'");
        DB.insert("e3", "id, e2_id, name", "7, 1, 'b'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"sort\":\"name\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":["
                + "{\"id\":7,\"name\":\"b\",\"phoneNumber\":null}," + "{\"id\":9,\"name\":\"s\",\"phoneNumber\":null},"
                + "{\"id\":8,\"name\":\"z\",\"phoneNumber\":null}]}],\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void test_ToMany_SortPath() {

        DB.insert("e5", "id, name", "145, 'B'");
        DB.insert("e5", "id, name", "143, 'D'");
        DB.insert("e5", "id, name", "146, 'A'");
        DB.insert("e2", "id, name", "11, 'xxx'");
        DB.insert("e3", "id, e2_id, e5_id, name", "18, 11, 145, 's'");
        DB.insert("e3", "id, e2_id, e5_id, name", "19, 11, 143, 'z'");
        DB.insert("e3", "id, e2_id, e5_id, name", "17, 11, 146, 'b'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"sort\":[{\"property\":\"e5.name\"}]}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":11,\"e3s\":["
                + "{\"id\":17,\"name\":\"b\",\"phoneNumber\":null},"
                + "{\"id\":18,\"name\":\"s\",\"phoneNumber\":null},"
                + "{\"id\":19,\"name\":\"z\",\"phoneNumber\":null}]}],\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void test_ToMany_SortPath_Dir() {

        DB.insert("e5", "id, name", "245, 'B'");
        DB.insert("e5", "id, name", "246, 'A'");
        DB.insert("e2", "id, name", "21, 'xxx'");
        DB.insert("e3", "id, e2_id, e5_id, name", "28, 21, 245, 's'");
        DB.insert("e3", "id, e2_id, e5_id, name", "29, 21, 245, 'z'");
        DB.insert("e3", "id, e2_id, e5_id, name", "27, 21, 246, 'b'");

        Response r = target("/e2")
                .queryParam(
                        "include",
                        urlEnc("{\"path\":\"e3s\",\"sort\":[{\"property\":\"e5.name\", \"direction\":\"DESC\"},{\"property\":\"name\", \"direction\":\"DESC\"}]}"))
                .queryParam("include", "id")
                .request()
                .get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":21,\"e3s\":["
                + "{\"id\":29,\"name\":\"z\",\"phoneNumber\":null},"
                + "{\"id\":28,\"name\":\"s\",\"phoneNumber\":null},"
                + "{\"id\":27,\"name\":\"b\",\"phoneNumber\":null}]}],\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void test_ToMany_CayenneExp() {

        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, name", "8, 1, 'a'");
        DB.insert("e3", "id, e2_id, name", "9, 1, 'z'");
        DB.insert("e3", "id, e2_id, name", "7, 1, 'a'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"cayenneExp\":{\"exp\":\"name = $n\", \"params\":{\"n\":\"a\"}}}"))
                .queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":["
                + "{\"id\":8,\"name\":\"a\",\"phoneNumber\":null},"
                + "{\"id\":7,\"name\":\"a\",\"phoneNumber\":null}]}],\"total\":1}", r.readEntity(String.class));
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

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":51,\"e3s\":["
                + "{\"id\":57,\"name\":\"b\",\"phoneNumber\":null}]}],\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void test_ToMany_Exclude() {

        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, name", "8, 1, 'a'");
        DB.insert("e3", "id, e2_id, name", "9, 1, 'z'");
        DB.insert("e3", "id, e2_id, name", "7, 1, 'm'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\"}"))
                .queryParam("include", "id")
                .queryParam("exclude", "e3s.id")
                .queryParam("exclude", "e3s.phoneNumber")
                .request().get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":[{\"name\":\"a\"},{\"name\":\"z\"},{\"name\":\"m\"}]}],\"total\":1}",
                r.readEntity(String.class));
    }

    @Test
    public void test_ToMany_IncludeRelated() {

        DB.insert("e5", "id, name", "345, 'B'");
        DB.insert("e5", "id, name", "346, 'A'");
        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, e5_id, name", "8, 1, 345, 'a'");
        DB.insert("e3", "id, e2_id, e5_id, name", "9, 1, 345, 'z'");
        DB.insert("e3", "id, e2_id, e5_id, name", "7, 1, 346, 'm'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\"}"))
                .queryParam("include", "id")
                .queryParam("exclude", "e3s.id")
                .queryParam("exclude", "e3s.phoneNumber")
                .queryParam("include", "e3s.e5.name").request().get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":[{\"e5\":{\"name\":\"B\"},\"name\":\"a\"},"
                        + "{\"e5\":{\"name\":\"B\"},\"name\":\"z\"},{\"e5\":{\"name\":\"A\"},\"name\":\"m\"}]}],\"total\":1}",
                r.readEntity(String.class));
    }

    @Test
    public void test_ToMany_IncludeArrayRelated() {

        DB.insert("e5", "id, name", "345, 'B'");
        DB.insert("e5", "id, name", "346, 'A'");
        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, e5_id, name", "8, 1, 345, 'a'");
        DB.insert("e3", "id, e2_id, e5_id, name", "9, 1, 345, 'z'");
        DB.insert("e3", "id, e2_id, e5_id, name", "7, 1, 346, 'm'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("[\"id\", {\"path\":\"e3s\"}, \"e3s.e5.name\"]"))
                .queryParam("exclude", urlEnc("[\"e3s.id\", \"e3s.phoneNumber\"]"))
                .request()
                .get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":[{\"e5\":{\"name\":\"B\"},\"name\":\"a\"},"
                + "{\"e5\":{\"name\":\"B\"},\"name\":\"z\"},"
                + "{\"e5\":{\"name\":\"A\"},\"name\":\"m\"}]}],\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void test_ToMany_IncludeMapRelated() {

        DB.insert("e5", "id, name", "345, 'B'");
        DB.insert("e5", "id, name", "346, 'A'");
        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, e5_id, name", "8, 1, 345, 'a'");
        DB.insert("e3", "id, e2_id, e5_id, name", "9, 1, 345, 'z'");
        DB.insert("e3", "id, e2_id, e5_id, name", "7, 1, 346, 'm'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("[\"id\", {\"path\":\"e3s\"}, {\"e3s.e5\":[\"name\"]}]"))
                .queryParam("exclude", urlEnc("[{\"e3s\": [\"id\", \"phoneNumber\"]}]"))
                .request().get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":[{\"e5\":{\"name\":\"B\"},\"name\":\"a\"},"
                + "{\"e5\":{\"name\":\"B\"},\"name\":\"z\"},"
                + "{\"e5\":{\"name\":\"A\"},\"name\":\"m\"}]}],\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void test_ToMany_IncludeExtMapRelated() {

        DB.insert("e5", "id, name", "345, 'B'");
        DB.insert("e5", "id, name", "346, 'A'");
        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e3", "id, e2_id, e5_id, name", "8, 1, 345, 'a'");
        DB.insert("e3", "id, e2_id, e5_id, name", "9, 1, 345, 'z'");
        DB.insert("e3", "id, e2_id, e5_id, name", "7, 1, 346, 'm'");

        Response r = target("/e2")
                .queryParam("include", urlEnc("[\"id\", {\"path\":\"e3s\", \"include\":[\"e5.name\"]}]"))
                .queryParam("exclude", urlEnc("[{\"e3s\": [\"id\", \"phoneNumber\"]}]"))
                .request().get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e3s\":[{\"e5\":{\"name\":\"B\"},\"name\":\"a\"},"
                + "{\"e5\":{\"name\":\"B\"},\"name\":\"z\"},"
                + "{\"e5\":{\"name\":\"A\"},\"name\":\"m\"}]}],\"total\":1}", r.readEntity(String.class));
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

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E4.class).uri(uriInfo).get();
        }
    }
}
