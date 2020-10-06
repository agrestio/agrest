package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.it.fixture.cayenne.E5;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_IncludeObjectIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E4.class, E5.class};
    }

    @Test
    public void testPathAttribute() {

        e4().insertColumns("c_int").values(55).exec();

        Response r = target("/e4").queryParam("include", urlEnc("{\"path\":\"cInt\"}")).request().get();
        onSuccess(r).bodyEquals(1, "{\"cInt\":55}");
    }

    @Test
    public void testPathRelationship() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id").values(8, "yyy", 1).exec();

        Response r = target("/e3")
                .queryParam("include", urlEnc("{\"path\":\"e2\"}"))
                .queryParam("include", "id")
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"}}");
    }

    @Test
    public void testMapBy_ToOne() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id").values(8, "yyy", 1).exec();

        Response r = target("/e3")
                .queryParam("include", urlEnc("{\"path\":\"e2\",\"mapBy\":\"name\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        // no support for MapBy for to-one... simply ignoring it...
        onSuccess(r).bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"}}");
    }

    @Test
    public void testMapBy_ToMany() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa", 1)
                .values(9, "zzz", 1)
                .values(7, "aaa", 1).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"name\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":{"
                + "\"aaa\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}],"
                + "\"zzz\":[{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}]" + "}}");
    }

    @Test
    public void testMapBy_ToMany_ById() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa", 1)
                .values(9, "zzz", 1)
                .values(7, "aaa", 1).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"id\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":{"
                + "\"8\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null}],"
                + "\"9\":[{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}],"
                + "\"7\":[{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}]}}");
    }

    @Test
    public void testMapBy_ToMany_ByRelatedId() {

        e5().insertColumns("id").values(45).values(46).exec();
        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(8, "aaa", 1, 45)
                .values(9, "zzz", 1, 45)
                .values(7, "aaa", 1, 46).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"e5.id\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":{"
                + "\"45\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}],"
                + "\"46\":[{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}]}}");
    }

    @Test
    public void testMapBy_ToMany_ByRelatedAttribute() {

        e5().insertColumns("id", "name")
                .values(45, "T")
                .values(46, "Y").exec();

        e2().insertColumns("id_", "name").values(1, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(8, "aaa", 1, 45)
                .values(9, "zzz", 1, 45)
                .values(7, "aaa", 1, 46).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"e5.name\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":{"
                + "\"T\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}],"
                + "\"Y\":[{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}]}}");
    }

    @Test
    public void testMapBy_ToMany_ByRelatedDate() {

        e5().insertColumns("id", "name", "date")
                .values(45, "T", "2013-01-03")
                .values(46, "Y", "2013-01-04").exec();

        e2().insertColumns("id_", "name").values(1, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(8, "aaa", 1, 45)
                .values(9, "zzz", 1, 45)
                .values(7, "aaa", 1, 46).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"e5.date\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":{"
                + "\"2013-01-03T00:00:00\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}],"
                + "\"2013-01-04T00:00:00\":[{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}]}}");
    }

    @Test
    public void testMapBy_ToMany_WithCayenneExp() {

        // see LF-294 - filter applied too late may cause a AgException

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "aaa", 1)
                .values(9, "zzz", 1)
                .values(7, "aaa", 1)
                .values(6, null, 1).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"mapBy\":\"name\", \"cayenneExp\":{\"exp\":\"name != NULL\"}}"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":{"
                + "\"aaa\":[{\"id\":8,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":7,\"name\":\"aaa\",\"phoneNumber\":null}],"
                + "\"zzz\":[{\"id\":9,\"name\":\"zzz\",\"phoneNumber\":null}]}}");
    }

    @Test
    public void testToMany_Sort() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "z", 1)
                .values(9, "s", 1)
                .values(7, "b", 1).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"sort\":\"name\"}"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":["
                + "{\"id\":7,\"name\":\"b\",\"phoneNumber\":null},"
                + "{\"id\":9,\"name\":\"s\",\"phoneNumber\":null},"
                + "{\"id\":8,\"name\":\"z\",\"phoneNumber\":null}]}");
    }

    @Test
    public void testToMany_SortPath() {

        e5().insertColumns("id", "name")
                .values(145, "B")
                .values(143, "D")
                .values(146, "A").exec();

        e2().insertColumns("id_", "name").values(11, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(18, "s", 11, 145)
                .values(19, "z", 11, 143)
                .values(17, "b", 11, 146).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"sort\":[{\"property\":\"e5.name\"}]}"))
                .queryParam("include", "id")
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":11,\"e3s\":["
                + "{\"id\":17,\"name\":\"b\",\"phoneNumber\":null},"
                + "{\"id\":18,\"name\":\"s\",\"phoneNumber\":null},"
                + "{\"id\":19,\"name\":\"z\",\"phoneNumber\":null}]}");
    }

    @Test
    public void testToMany_SortPath_Dir() {

        e5().insertColumns("id", "name")
                .values(245, "B")
                .values(246, "A").exec();

        e2().insertColumns("id_", "name").values(21, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(28, "s", 21, 245)
                .values(29, "z", 21, 245)
                .values(27, "b", 21, 246).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"sort\":[{\"property\":\"e5.name\", \"direction\":\"DESC\"},{\"property\":\"name\", \"direction\":\"DESC\"}]}"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":21,\"e3s\":["
                + "{\"id\":29,\"name\":\"z\",\"phoneNumber\":null},"
                + "{\"id\":28,\"name\":\"s\",\"phoneNumber\":null},"
                + "{\"id\":27,\"name\":\"b\",\"phoneNumber\":null}]}");
    }

    @Test
    public void testToMany_CayenneExp() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "a", 1)
                .values(9, "z", 1)
                .values(7, "a", 1).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"cayenneExp\":{\"exp\":\"name = $n\", \"params\":{\"n\":\"a\"}}}"))
                .queryParam("include", "id")
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":["
                + "{\"id\":8,\"name\":\"a\",\"phoneNumber\":null},"
                + "{\"id\":7,\"name\":\"a\",\"phoneNumber\":null}]}");
    }

    @Test
    public void testToMany_CayenneExpById() {

        e5().insertColumns("id", "name")
                .values(545, "B")
                .values(546, "A").exec();

        e2().insertColumns("id_", "name").values(51, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(58, "s", 51, 545)
                .values(59, "z", 51, 545)
                .values(57, "b", 51, 546).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\",\"cayenneExp\":{\"exp\":\"e5 = $id\", \"params\":{\"id\":546}}}"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":51,\"e3s\":[{\"id\":57,\"name\":\"b\",\"phoneNumber\":null}]}");
    }

    @Test
    public void testToMany_Exclude() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "a", 1)
                .values(9, "z", 1)
                .values(7, "m", 1).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\"}"))
                .queryParam("include", "id")
                .queryParam("exclude", "e3s.id")
                .queryParam("exclude", "e3s.phoneNumber")
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":[{\"name\":\"a\"},{\"name\":\"z\"},{\"name\":\"m\"}]}");
    }

    @Test
    public void testToMany_IncludeRelated() {

        e5().insertColumns("id", "name")
                .values(345, "B")
                .values(346, "A").exec();

        e2().insertColumns("id_", "name").values(1, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(8, "a", 1, 345)
                .values(9, "z", 1, 345)
                .values(7, "m", 1, 346).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\"}"))
                .queryParam("include", "id")
                .queryParam("exclude", "e3s.id")
                .queryParam("exclude", "e3s.phoneNumber")
                .queryParam("include", "e3s.e5.name").request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":[{\"e5\":{\"name\":\"B\"},\"name\":\"a\"},"
                + "{\"e5\":{\"name\":\"B\"},\"name\":\"z\"},{\"e5\":{\"name\":\"A\"},\"name\":\"m\"}]}");
    }

    @Test
    public void testToMany_IncludeArrayRelated() {

        e5().insertColumns("id", "name")
                .values(345, "B")
                .values(346, "A").exec();

        e2().insertColumns("id_", "name").values(1, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(8, "a", 1, 345)
                .values(9, "z", 1, 345)
                .values(7, "m", 1, 346).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("[\"id\", {\"path\":\"e3s\"}, \"e3s.e5.name\"]"))
                .queryParam("exclude", urlEnc("[\"e3s.id\", \"e3s.phoneNumber\"]"))
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":[{\"e5\":{\"name\":\"B\"},\"name\":\"a\"},"
                + "{\"e5\":{\"name\":\"B\"},\"name\":\"z\"},"
                + "{\"e5\":{\"name\":\"A\"},\"name\":\"m\"}]}");
    }

    @Test
    public void testToMany_IncludeMapRelated() {

        e5().insertColumns("id", "name")
                .values(345, "B")
                .values(346, "A").exec();

        e2().insertColumns("id_", "name").values(1, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(8, "a", 1, 345)
                .values(9, "z", 1, 345)
                .values(7, "m", 1, 346).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("[\"id\", {\"path\":\"e3s\"}, {\"e3s.e5\":[\"name\"]}]"))
                .queryParam("exclude", urlEnc("[{\"e3s\": [\"id\", \"phoneNumber\"]}]"))
                .request()
                .get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":[{\"e5\":{\"name\":\"B\"},\"name\":\"a\"},"
                + "{\"e5\":{\"name\":\"B\"},\"name\":\"z\"},"
                + "{\"e5\":{\"name\":\"A\"},\"name\":\"m\"}]}");
    }

    @Test
    public void testToMany_IncludeExtMapRelated() {

        e5().insertColumns("id", "name")
                .values(345, "B")
                .values(346, "A").exec();

        e2().insertColumns("id_", "name").values(1, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(8, "a", 1, 345)
                .values(9, "z", 1, 345)
                .values(7, "m", 1, 346).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("[\"id\", {\"path\":\"e3s\", \"include\":[\"e5.name\"]}]"))
                .queryParam("exclude", urlEnc("[{\"e3s\": [\"id\", \"phoneNumber\"]}]"))
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":[{\"e5\":{\"name\":\"B\"},\"name\":\"a\"},"
                + "{\"e5\":{\"name\":\"B\"},\"name\":\"z\"},"
                + "{\"e5\":{\"name\":\"A\"},\"name\":\"m\"}]}");
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
