package io.agrest.it;

import io.agrest.it.fixture.BQJerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.swagger.api.v1.service.E3Resource;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class E3Resource_GeneratedIT extends BQJerseyTestOnDerby {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(E3Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class};
    }

    @Test
    public void testGET_ById_Include() {

        e2().insertColumns("id", "name").values(1, "xxx").exec();

        e3().insertColumns("id", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r1 = target("/v1/e3/8").queryParam("include", "e2.id").request().get();
        onSuccess(r1).bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1},\"name\":\"yyy\",\"phoneNumber\":null}");

        Response r2 = target("/v1/e3/8").queryParam("include", "e2.name").request().get();
        onSuccess(r2).bodyEquals(1, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"name\":\"yyy\",\"phoneNumber\":null}");
    }

    @Test
    public void testGET_Include_Sort_Dir() {

        e2().insertColumns("id", "name").values(1, "xxx").exec();

        e3().insertColumns("id", "name", "e2_id")
                .values(9, "zzz", 1)
                .values(8, "yyy", 1).exec();

        Response r1 = target("/v1/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("dir", "ASC")
                .request().get();

        onSuccess(r1).bodyEquals(2, "{\"id\":8,\"e2\":{\"id\":1}}", "{\"id\":9,\"e2\":{\"id\":1}}");

        Response r2 = target("/v1/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("dir", "DESC")
                .request().get();

        onSuccess(r2).bodyEquals(2, "{\"id\":9,\"e2\":{\"id\":1}}", "{\"id\":8,\"e2\":{\"id\":1}}");
    }

    @Test
    public void testGET_Start_Limit() {

        e2().insertColumns("id", "name").values(1, "xxx").exec();

        e3().insertColumns("id", "name", "e2_id")
                .values(10, "zzz", 1)
                .values(9, "zzz", 1)
                .values(8, "yyy", 1)
                .values(11, "zzz", 1).exec();

        Response r = target("/v1/e3")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .queryParam("start", "1")
                .queryParam("limit", "2")
                .request()
                .get();

        onSuccess(r).bodyEquals(4,
                "{\"id\":9,\"e2\":{\"id\":1}}",
                "{\"id\":10,\"e2\":{\"id\":1}}");
    }

    @Test
    public void testGET_CayenneExp() {

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzzz").exec();

        e3().insertColumns("id", "name", "e2_id")
                .values(6, "yyy", 3)
                .values(8, "yyy", 1)
                .values(9, "zzz", 2).exec();

        Response r = target("/v1/e3")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"e2 in $ids\",\"params\":{\"ids\": [3, 4]}}"))
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":6}");
    }

    @Test
    public void testGET_RelatedAll_Include() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/v1/e3/7/e2").queryParam("include", "id").request().get();
        onSuccess(r).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testPOST() {

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();


        Response r = target("/v1/e3").request()
                .post(Entity.json("{\"e2\":8,\"name\":\"MM\"}"));

        onResponse(r).statusEquals(Response.Status.CREATED)
                .replaceId("XID").bodyEquals(1, "{\"id\":XID,\"name\":\"MM\",\"phoneNumber\":null}");
    }

    @Test
    public void testPUT_ById() {

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        e3().insertColumns("id", "name", "e2_id").values(3, "zzz", 8).exec();

        Response response = target("/v1/e3/3")
                .request()
                .put(Entity.json("{\"id\":3,\"e2\":1}"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");
        e3().matcher().eq("id", 3).eq("e2_id", 1).assertOneMatch();
    }


    @Test
    public void testPUT_Bulk() {

        e3().insertColumns("id", "name")
                .values(5, "aaa")
                .values(4, "zzz")
                .values(2, "bbb")
                .values(6, "yyy").exec();

        Entity<String> entity = Entity.json(
                "[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]");
        Response response = target("/v1/e3/")
                .queryParam("exclude", "id")
                .queryParam("include", "name")
                .request()
                .put(entity);

        // ordering must be preserved in response, so comparing with request entity
        onSuccess(response).bodyEquals(4,
                "{\"name\":\"yyy\"}",
                "{\"name\":\"zzz\"}",
                "{\"name\":\"111\"}",
                "{\"name\":\"333\"}");
    }

    @Test
    public void testPUT_Relate() {

        e2().insertColumns("id", "name")
                .values(24, "xxx").exec();

        e3().insertColumns("id", "name")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        // PUT with empty body ... how bad is that?
        Response r = target("/v1/e3/8/e2/24").request().put(Entity.json(""));

        onSuccess(r).bodyEquals(1, "{\"id\":24,\"address\":null,\"name\":\"xxx\"}");

        e3().matcher().eq("e2_id", 24).eq("name", "yyy").assertOneMatch();
    }

    @Test
    public void testDELETE_UnrelateById() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/v1/e3/9/e2/1").request().delete();
        onSuccess(r).bodyEquals("{\"success\":true}");

        // this delete is really "unrelate", so the records will be there, but their FKs will be set to null
        e3().matcher().assertMatches(3);
        e3().matcher().eq("e2_id", 1).assertOneMatch();
    }


    @Test
    public void testDELETE_UnrelateAll() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/v1/e3/9/e2").request().delete();
        onSuccess(r).bodyEquals("{\"success\":true}");

        // this delete is really "unrelate", so the records will be there, but their FKs will be set to null
        e3().matcher().assertMatches(3);
        e3().matcher().eq("e2_id", 1).assertOneMatch();
    }
}
