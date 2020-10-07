package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.*;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.runtime.processor.update.ByKeyObjectMapperFactory;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

public class PUT_Related_ByKeyIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E7.class, E8.class, E14.class, E15.class)
            .build();

    @Test
    public void testRelate_ToMany_MixedCollection() {

        tester.e8().insertColumns("id", "name")
                .values(15, "xxx")
                .values(16, "xxx").exec();

        tester.e7().insertColumns("id", "name", "e8_id")
                .values(7, "zzz", 16)
                .values(8, "yyy", 15)
                .values(9, "aaa", 15).exec();

        tester.target("/e8/bykey/15/e7s")
                .put("[  {\"name\":\"newname\"}, {\"name\":\"aaa\"} ]")
                .wasOk()
                .replaceId("XID")
                .bodyEquals(2, "{\"id\":XID,\"name\":\"newname\"},{\"id\":9,\"name\":\"aaa\"}");

        tester.e7().matcher().assertMatches(4);

        // testing idempotency

        tester.target("/e8/bykey/15/e7s")
                .put("[  {\"name\":\"newname\"}, {\"name\":\"aaa\"} ]")
                .wasOk().replaceId("XID")
                .bodyEquals(2,
                        "{\"id\":XID,\"name\":\"newname\"}",
                        "{\"id\":9,\"name\":\"aaa\"}");

        tester.e7().matcher().assertMatches(4);
    }

    @Test
    public void testRelate_ToMany_PropertyMapper() {

        tester.e8().insertColumns("id", "name")
                .values(15, "xxx")
                .values(16, "xxx").exec();

        tester.e7().insertColumns("id", "name", "e8_id")
                .values(7, "zzz", 16)
                .values(8, "yyy", 15)
                .values(9, "aaa", 15).exec();

        tester.target("/e8/bypropkey/15/e7s")
                .put("[  {\"name\":\"newname\"}, {\"name\":\"aaa\"} ]")
                .wasOk().replaceId("XID")
                .bodyEquals(2, "{\"id\":XID,\"name\":\"newname\"},{\"id\":9,\"name\":\"aaa\"}");

        tester.e7().matcher().assertMatches(4);
    }

    @Test
    public void testToMany_LongId() {

        tester.e15().insertColumns("long_id", "name")
                .values(5L, "aaa")
                .values(44L, "aaa").exec();

        tester.e14().insertColumns("long_id", "e15_id", "name")
                .values(5L, 5L, "aaa")
                .values(4L, 44L, "zzz")
                .values(2L, 44L, "bbb")
                .values(6L, 5L, "yyy").exec();

        tester.target("/e15/44/e14s").queryParam("exclude", "id").queryParam("include", E3.NAME.getName())
                .put("[{\"id\":4,\"name\":\"zzz\"},{\"id\":11,\"name\":\"new\"}]")
                .wasOk()
                // update: ordering must be preserved...
                .bodyEquals(2,
                        "{\"id\":4,\"name\":\"zzz\",\"prettyName\":\"zzz_pretty\"}",
                        "{\"id\":11,\"name\":\"new\",\"prettyName\":\"new_pretty\"}");

        tester.e14().matcher().eq("e15_id", 44).assertMatches(2);

        // TODO: checking individual records one by one until "in()" becomes available in BQ 1.1 per
        //  https://github.com/bootique/bootique-jdbc/issues/92
        tester.e14().matcher().eq("e15_id", 44).eq("long_id", 4L).assertOneMatch();
        tester.e14().matcher().eq("e15_id", 44).eq("long_id", 11L).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e8/bykey/{id}/e7s")
        public DataResponse<E7> e8CreateOrUpdateE7sByKey_Idempotent(@PathParam("id") int id, String entityData) {
            return Ag.idempotentCreateOrUpdate(E7.class, config)
                    .mapper(ByKeyObjectMapperFactory.byKey(E7.NAME))
                    .toManyParent(E8.class, id, E8.E7S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e8/bypropkey/{id}/e7s")
        public DataResponse<E7> e8CreateOrUpdateE7sByPropKey_Idempotent(@PathParam("id") int id, String entityData) {
            return Ag.idempotentCreateOrUpdate(E7.class, config).mapper(E7.NAME).toManyParent(E8.class, id, E8.E7S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e15/{id}/e14s")
        // note that parent id is "int" here , but is BIGINT (long) in the DB. This
        // is intentional
        public DataResponse<E14> relateToOneExisting(@PathParam("id") int id, String data) {
            return Ag.idempotentFullSync(E14.class, config).toManyParent(E15.class, id, E15.E14S).syncAndSelect(data);
        }
    }
}
