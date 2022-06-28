package io.agrest.jpa;


import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.E3;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
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
    static final AgJpaTester tester = tester(Resource.class)

            .entities(E3.class)
            .build();

    @Test
    public void testSingle() {

        tester.e3().insertColumns("ID", "NAME").values(3, "zzz").exec();

        tester.target("/e3/updatebinding/3")
                .put("{\"id\":3,\"name\":\"yyy\"}")
                .wasOk().bodyEquals("{}");

        tester.e3().matcher().eq("ID", 3).eq("NAME", "yyy").assertOneMatch();
    }

    @Test
    public void testCollection() {

        tester.e3().insertColumns("ID", "NAME")
                .values(3, "zzz")
                .values(4, "xxx")
                .values(5, "mmm").exec();

        tester.target("/e3/updatebinding")
                .put("[{\"id\":3,\"name\":\"yyy\"},{\"id\":5,\"name\":\"nnn\"}]")
                .wasOk().bodyEquals("{}");

        tester.e3().matcher().assertMatches(2);
        tester.e3().matcher().eq("ID", 3).eq("NAME", "yyy").assertOneMatch();
        tester.e3().matcher().eq("ID", 5).eq("NAME", "nnn").assertOneMatch();
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
