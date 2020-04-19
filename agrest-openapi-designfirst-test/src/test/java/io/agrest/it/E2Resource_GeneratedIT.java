package io.agrest.it;


import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.swagger.api.v1.service.E2Resource;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.junit.Assert.*;

public class E2Resource_GeneratedIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(E2Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class};
    }

    @Test
    public void testGET_ById_Include() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/v1/e2/1")
                .queryParam("include", "e3s.id")
                .request().get();
        onSuccess(r).bodyEquals(1, "{\"id\":1,\"address\":null,\"e3s\":[{\"id\":8},{\"id\":9}],\"name\":\"xxx\"}");
    }

    @Test
    public void testGET_CayenneExp() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        Response r = target("/v1/e2")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("name = 'yyy'")).request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testGET_RelatedAll_Include() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/v1/e2/1/e3s")
                .queryParam("include", "id")
                .request().get();

        onSuccess(r).bodyEquals(2, "{\"id\":8},{\"id\":9}");
    }


    @Test
    public void testGET_RelatedById_Include() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/v1/e2/1/e3s/8")
                .queryParam("include", "id")
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":8}");
    }


    @Test
    public void testPOST_IncludeExclude() {

        e3().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        Response response = target("/v1/e2")
                .queryParam("include", "e3s")
                .queryParam("exclude", "address", "e3s.name", "e3s.phoneNumber")
                .request()
                .post(Entity.json("{\"e3s\":[1,8],\"name\":\"MM\"}"));

        onResponse(response)
                .statusEquals(Response.Status.CREATED)
                .replaceId("XID")
                .bodyEquals(1, "{\"id\":XID,\"e3s\":[{\"id\":1},{\"id\":8}],\"name\":\"MM\"}");


        // TODO: hopefully there will be a an easier way to select ID per
        //   https://github.com/bootique/bootique-jdbc/issues/93
        List<Object[]> ids = e2().selectColumns("id_");
        assertEquals(1, ids.size());
        int id = (Integer) ids.get(0)[0];

        e3().matcher().eq("e2_id", id).assertMatches(2);
    }

    @Test
    public void testPOST_Relate() {

        e2().insertColumns("id_", "name").values(24, "xxx").exec();

        Response r = target("/v1/e2/24/e3s")
                .request()
                .post(Entity.json("{\"name\":\"zzz\"}"));

        onSuccess(r).replaceId("XID").bodyEquals(1, "{\"id\":XID,\"name\":\"zzz\",\"phoneNumber\":null}");
        e3().matcher().assertOneMatch();
    }

    @Test
    public void testPUT_ById() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        Response response = target("/v1/e2/1")
                .queryParam("include", "e3s")
                .queryParam("exclude", "address", "name", "e3s.name", "e3s.phoneNumber")
                .request().put(Entity.json("{\"e3s\":[3,4,5]}"));

        onSuccess(response).bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":3},{\"id\":4},{\"id\":5}]}");
        e3().matcher().eq("e2_id", 1).assertMatches(3);
    }

    @Test
    public void testDELETE_UnrelateById() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.


        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/v1/e2/1/e3s/9").request().delete();
        onSuccess(r).bodyEquals("{\"success\":true}");

        // this delete is really "unrelate", so the records will be there, but their FKs will be set to null
        e3().matcher().assertMatches(3);
        e3().matcher().eq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void testDELETE_UnrelateAll() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/v1/e2/1/e3s").request().delete();
        onSuccess(r).bodyEquals("{\"success\":true}");

        // this delete is really "unrelate", so the records will be there, but their FKs will be set to null
        e3().matcher().assertMatches(3);
        e3().matcher().eq("e2_id", 1).assertNoMatches();
    }
}
