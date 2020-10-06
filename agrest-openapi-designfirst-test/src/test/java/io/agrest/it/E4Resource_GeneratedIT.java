package io.agrest.it;


import io.agrest.cayenne.unit.DbTest;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.swagger.api.v1.service.E4Resource;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class E4Resource_GeneratedIT extends DbTest {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(E4Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E4.class};
    }

    @Test
    public void testGET() {

        e4().insertColumns("id", "c_varchar", "c_int")
                .values(1, "xxx", 5).exec();

        Response r = target("/v1/e4").request().get();
        onSuccess(r).bodyEquals(1,
                "{\"id\":1,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":5,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"xxx\"}");
    }

    @Test
    public void testGET_Include() {

        e4().insertColumns("id", "c_varchar", "c_int")
                .values(1, "xxx", 5).exec();

        Response r = target("/v1/e4").queryParam("include", "id", "cInt").request().get();
        onSuccess(r).bodyEquals(1, "{\"id\":1,\"cInt\":5}");
    }

    @Test
    public void testGET_Sort() {

        e4().insertColumns("id")
                .values(2)
                .values(1)
                .values(3).exec();

        Response response = target("/v1/e4")
                .queryParam("sort", urlEnc("[{\"property\":\"id\",\"direction\":\"DESC\"}]"))
                .queryParam("include", "id")
                .request()
                .get();

        onSuccess(response).bodyEquals(3, "{\"id\":3}", "{\"id\":2}", "{\"id\":1}");
    }

    @Test
    public void testGET_Sort_DirIgnored() {

        e4().insertColumns("id")
                .values(2)
                .values(1)
                .values(3).exec();

        Response response = target("/v1/e4")
                .queryParam("sort", "id")
                .queryParam("dir", "DESC")
                .queryParam("include", "id")
                .request().get();

        // "dir" must be ignored as it is not a part of the method signature
        onSuccess(response).bodyEquals(3, "{\"id\":1}", "{\"id\":2}", "{\"id\":3}");
    }


    @Test
    public void testGET_ById() {

        e4().insertColumns("id")
                .values(2)
                .values(1)
                .values(3).exec();

        Response response = target("/v1/e4/2")
                .queryParam("include", "id")
                .request().get();

        onSuccess(response).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testGET_MapBy() {

        e4().insertColumns("c_varchar", "c_int")
                .values("xxx", 1)
                .values("yyy", 2)
                .values("zzz", 2).exec();

        Response response = target("/v1/e4")
                .queryParam("mapBy", "cInt")
                .queryParam("include", "cVarchar")
                .request().get();

        onSuccess(response).bodyEqualsMapBy(3,
                "\"1\":[{\"cVarchar\":\"xxx\"}]",
                "\"2\":[{\"cVarchar\":\"yyy\"},{\"cVarchar\":\"zzz\"}]");
    }

    @Test
    public void testPOST() {

        Response r = target("/v1/e4").request()
                .post(Entity.json("{\"cVarchar\":\"zzz\"}"));

        e4().matcher().assertOneMatch();
        onResponse(r).statusEquals(Response.Status.CREATED).replaceId("XID")
                .bodyEquals(1, "{\"id\":XID,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
                        + "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"zzz\"}");
    }

    @Test
    public void testPUT() {

        e4().insertColumns("id", "c_varchar")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        Response r = target("/v1/e4/8").request().put(Entity.json("{\"id\":8,\"cVarchar\":\"zzz\"}"));

        onResponse(r).bodyEquals(1, "{\"id\":8," +
                "\"cBoolean\":null," +
                "\"cDate\":null," +
                "\"cDecimal\":null," +
                "\"cInt\":null," +
                "\"cTime\":null," +
                "\"cTimestamp\":null," +
                "\"cVarchar\":\"zzz\"}");

        e4().matcher().eq("id", 8).eq("c_varchar", "zzz").assertOneMatch();
    }

    @Test
    public void testDelete() {

        e4().insertColumns("id", "c_varchar")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        Response r = target("/v1/e4/8").request().delete();
        onResponse(r).statusEquals(Response.Status.OK).bodyEquals("{\"success\":true}");

        e4().matcher().assertOneMatch();
    }

}
