package io.agrest.cayenne.PUT;


import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collection;

public class EntityUpdateBindingIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)

            .entities(E3.class)
            .build();

    @Test
    public void single() {

        tester.e3().insertColumns("id_", "name").values(3, "zzz").exec();

        tester.target("/e3/updatebinding/3")
                .put("{\"id\":3,\"name\":\"yyy\"}")
                .wasOk().bodyEquals("{}");

        tester.e3().matcher().eq("id_", 3).eq("name", "yyy").assertOneMatch();
    }

    @Test
    public void collection() {

        tester.e3().insertColumns("id_", "name")
                .values(3, "zzz")
                .values(4, "xxx")
                .values(5, "mmm").exec();

        tester.target("/e3/updatebinding")
                .put("[{\"id\":3,\"name\":\"yyy\"},{\"id\":5,\"name\":\"nnn\"}]")
                .wasOk().bodyEquals("{}");

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
