package io.agrest.sencha.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.sencha.it.fixture.SenchaBQJerseyTestOnDerby;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.junit.Assert.*;

public class Sencha_PUT_IT extends SenchaBQJerseyTestOnDerby {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class};
    }


    @Test
    public void testPut_ToOne_FromNull() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", null).exec();

        Response r = target("/e3/3").request().put(Entity.json("{\"id\":3,\"e2_id\":8}"));
        onSuccess(r).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        e3().matcher().eq("id_", 3).eq("e2_id", 8).assertOneMatch();
    }

    @Test
    public void testPut_ToOne_ToNull() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        Response r = target("/e3/3").request().put(Entity.json("{\"id\":3,\"e2_id\":null}"));
        onSuccess(r).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        // TODO: can't use matcher for NULLs until BQ 1.1 upgrade (because of https://github.com/bootique/bootique-jdbc/issues/91 )
        //  so using select...

        List<Object[]> rows = e3().selectColumns("id_", "e2_id");
        assertEquals(1, rows.size());
        assertEquals(3, rows.get(0)[0]);
        assertNull(rows.get(0)[1]);
    }

    @Test
    public void testPut_ToOne() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        Response r = target("/e3/3").request().put(Entity.json("{\"id\":3,\"e2_id\":1}"));
        onSuccess(r).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void testPut_ToOne_Relationship_Name() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", 8).exec();

        Response r = target("/e3/3").request().put(Entity.json("{\"id\":3,\"e2\":1}"));
        onSuccess(r).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e3/{id}")
        public DataResponse<E3> updateE3(@PathParam("id") int id, String requestBody) {
            return Ag.update(E3.class, config).id(id).syncAndSelect(requestBody);
        }
    }
}
