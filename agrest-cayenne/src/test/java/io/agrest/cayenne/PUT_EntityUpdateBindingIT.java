package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.jaxrs.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

public class PUT_EntityUpdateBindingIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)

            .entities(E3.class)
            .build();

    @Test
    public void testSingle() {

        tester.e3().insertColumns("id_", "name").values(3, "zzz").exec();

        tester.target("/e3/updatebinding/3")
                .put("{\"id\":3,\"name\":\"yyy\"}")
                .wasOk().bodyEquals("{\"success\":true}");

        tester.e3().matcher().eq("id_", 3).eq("name", "yyy").assertOneMatch();
    }

    @Test
    public void testCollection() {

        tester.e3().insertColumns("id_", "name")
                .values(3, "zzz")
                .values(4, "xxx")
                .values(5, "mmm").exec();

        tester.target("/e3/updatebinding")
                .put("[{\"id\":3,\"name\":\"yyy\"},{\"id\":5,\"name\":\"nnn\"}]")
                .wasOk().bodyEquals("{\"success\":true}");

        tester.e3().matcher().assertMatches(2);
        tester.e3().matcher().eq("id_", 3).eq("name", "yyy").assertOneMatch();
        tester.e3().matcher().eq("id_", 5).eq("name", "nnn").assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e3/updatebinding")
        public SimpleResponse sync_EntityUpdateCollection(
                @Context UriInfo uriInfo,
                Collection<EntityUpdate<E3>> entityUpdates) {
            return AgJaxrs.idempotentFullSync(E3.class, config).clientParams(uriInfo.getQueryParameters()).sync(entityUpdates);
        }

        @PUT
        @Path("e3/updatebinding/{id}")
        public SimpleResponse updateE3_EntityUpdateSingle(
                @PathParam("id") int id,
                EntityUpdate<E3> update) {

            return AgJaxrs.createOrUpdate(E3.class, config).byId(id).sync(update);
        }
    }
}
