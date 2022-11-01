package io.agrest.cayenne;

import io.agrest.DeleteStage;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DELETE_StagesIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class)
            .build();

    @BeforeEach
    public void resetCallbacks() {
        Resource.START_CALLED = false;
        Resource.MAP_CHANGES_CALLED = false;
        Resource.AUTHORIZE_CHANGES_CALLED = false;
        Resource.DELETE_IN_DATA_STORE_CALLED = false;
    }

    @Test
    public void testDeleteAll() {

        tester.e3().insertColumns("id_").values(3).values(4).exec();
        tester.target("/e3").delete().wasOk();

        tester.e3().matcher().assertNoMatches();

        assertTrue(Resource.START_CALLED);
        assertTrue(Resource.MAP_CHANGES_CALLED);
        assertTrue(Resource.AUTHORIZE_CHANGES_CALLED);
        assertTrue(Resource.DELETE_IN_DATA_STORE_CALLED);
    }

    @Test
    public void testDeleteNone() {

        tester.target("/e3").delete().wasOk();

        tester.e3().matcher().assertNoMatches();

        // delete chain quits early if no objects matched the deletion criteria
        assertTrue(Resource.START_CALLED);
        assertFalse(Resource.MAP_CHANGES_CALLED);
        assertFalse(Resource.AUTHORIZE_CHANGES_CALLED);
        assertFalse(Resource.DELETE_IN_DATA_STORE_CALLED);
    }

    @Path("")
    public static class Resource {

        public static boolean START_CALLED;
        public static boolean MAP_CHANGES_CALLED;
        public static boolean AUTHORIZE_CHANGES_CALLED;
        public static boolean DELETE_IN_DATA_STORE_CALLED;

        @Context
        private Configuration config;

        @DELETE
        @Path("e3")
        public SimpleResponse delete() {
            return AgJaxrs.delete(E3.class, config)
                    .stage(DeleteStage.START, this::onStart)
                    .stage(DeleteStage.MAP_CHANGES, this::onMapChanges)
                    .stage(DeleteStage.AUTHORIZE_CHANGES, this::onAuthorizeChanges)
                    .stage(DeleteStage.DELETE_IN_DATA_STORE, this::onDeleteInDataStore)
                    .sync();
        }

        private void onStart(DeleteContext<E3> context) {
            assertFalse(Resource.START_CALLED);
            assertFalse(Resource.MAP_CHANGES_CALLED);
            assertFalse(Resource.AUTHORIZE_CHANGES_CALLED);
            assertFalse(Resource.DELETE_IN_DATA_STORE_CALLED);

            START_CALLED = true;
        }

        private void onMapChanges(DeleteContext<E3> context) {
            assertTrue(Resource.START_CALLED);
            assertFalse(Resource.MAP_CHANGES_CALLED);
            assertFalse(Resource.AUTHORIZE_CHANGES_CALLED);
            assertFalse(Resource.DELETE_IN_DATA_STORE_CALLED);

            MAP_CHANGES_CALLED = true;
        }

        private void onAuthorizeChanges(DeleteContext<E3> context) {
            assertTrue(Resource.START_CALLED);
            assertTrue(Resource.MAP_CHANGES_CALLED);
            assertFalse(Resource.AUTHORIZE_CHANGES_CALLED);
            assertFalse(Resource.DELETE_IN_DATA_STORE_CALLED);

            AUTHORIZE_CHANGES_CALLED = true;
        }

        private void onDeleteInDataStore(DeleteContext<E3> context) {

            assertTrue(Resource.START_CALLED);
            assertTrue(Resource.MAP_CHANGES_CALLED);
            assertTrue(Resource.AUTHORIZE_CHANGES_CALLED);
            assertFalse(Resource.DELETE_IN_DATA_STORE_CALLED);

            DELETE_IN_DATA_STORE_CALLED = true;
        }
    }
}
