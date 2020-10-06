package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.UpdateStage;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.runtime.processor.update.UpdateContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.jupiter.api.Assertions.*;

public class PUT_StagesIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E3.class};
    }

    @Before
    public void resetCallbacks() {
        Resource.START_CALLED = false;
        Resource.PARSE_REQUEST_CALLED = false;
        Resource.CREATE_ENTITY_CALLED = false;
        Resource.APPLY_SERVER_PARAMS_CALLED = false;
        Resource.MERGE_CHANGES_CALLED = false;
        Resource.COMMIT_CALLED = false;
        Resource.FILL_RESPONSE_CALLED = false;
    }

    @Test
    public void testToOne() {

        e3().insertColumns("id_", "name")
                .values(3, "z")
                .values(4, "a").exec();

        Response response = target("/e3/callbackstage")
                .request()
                .put(Entity.json("[{\"id\":3,\"name\":\"x\"}]"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"x\",\"phoneNumber\":null}");

        e3().matcher().eq("id_", 3).eq("name", "x").assertOneMatch();
        e3().matcher().eq("id_", 4).assertNoMatches();

        assertTrue(Resource.START_CALLED);
        assertTrue(Resource.PARSE_REQUEST_CALLED);
        assertTrue(Resource.CREATE_ENTITY_CALLED);
        assertTrue(Resource.APPLY_SERVER_PARAMS_CALLED);
        assertTrue(Resource.MERGE_CHANGES_CALLED);
        assertTrue(Resource.COMMIT_CALLED);
        assertTrue(Resource.FILL_RESPONSE_CALLED);
    }

    @Path("")
    public static class Resource {

        public static boolean START_CALLED;
        public static boolean PARSE_REQUEST_CALLED;
        public static boolean CREATE_ENTITY_CALLED;
        public static boolean APPLY_SERVER_PARAMS_CALLED;
        public static boolean MERGE_CHANGES_CALLED;
        public static boolean COMMIT_CALLED;
        public static boolean FILL_RESPONSE_CALLED;

        @Context
        private Configuration config;

        @PUT
        @Path("e3/callbackstage")
        public DataResponse<E3> syncWithCallbackStage(@Context UriInfo uriInfo, String requestBody) {
            return Ag.idempotentFullSync(E3.class, config)
                    .stage(UpdateStage.START, this::onStart)
                    .stage(UpdateStage.PARSE_REQUEST, this::onParseRequest)
                    .stage(UpdateStage.CREATE_ENTITY, this::onCreateEntity)
                    .stage(UpdateStage.APPLY_SERVER_PARAMS, this::onApplyServerParams)
                    .stage(UpdateStage.MERGE_CHANGES, this::onMergeChanges)
                    .stage(UpdateStage.COMMIT, this::onCommit)
                    .stage(UpdateStage.FILL_RESPONSE, this::onFillResponse)
                    .uri(uriInfo)
                    .syncAndSelect(requestBody);
        }

        private void onStart(UpdateContext<E3> context) {
            assertFalse(Resource.START_CALLED);
            assertFalse(Resource.PARSE_REQUEST_CALLED);
            assertFalse(Resource.CREATE_ENTITY_CALLED);
            assertFalse(Resource.APPLY_SERVER_PARAMS_CALLED);
            assertFalse(Resource.MERGE_CHANGES_CALLED);
            assertFalse(Resource.COMMIT_CALLED);
            assertFalse(Resource.FILL_RESPONSE_CALLED);

            START_CALLED = true;
        }

        private void onParseRequest(UpdateContext<E3> context) {
            assertTrue(Resource.START_CALLED);
            assertFalse(Resource.PARSE_REQUEST_CALLED);
            assertFalse(Resource.CREATE_ENTITY_CALLED);
            assertFalse(Resource.APPLY_SERVER_PARAMS_CALLED);
            assertFalse(Resource.MERGE_CHANGES_CALLED);
            assertFalse(Resource.COMMIT_CALLED);
            assertFalse(Resource.FILL_RESPONSE_CALLED);

            PARSE_REQUEST_CALLED = true;
        }

        private void onCreateEntity(UpdateContext<E3> context) {
            assertTrue(Resource.START_CALLED);
            assertTrue(Resource.PARSE_REQUEST_CALLED);
            assertFalse(Resource.CREATE_ENTITY_CALLED);
            assertFalse(Resource.APPLY_SERVER_PARAMS_CALLED);
            assertFalse(Resource.MERGE_CHANGES_CALLED);
            assertFalse(Resource.COMMIT_CALLED);
            assertFalse(Resource.FILL_RESPONSE_CALLED);

            CREATE_ENTITY_CALLED = true;
        }

        private void onApplyServerParams(UpdateContext<E3> context) {
            assertTrue(Resource.START_CALLED);
            assertTrue(Resource.PARSE_REQUEST_CALLED);
            assertTrue(Resource.CREATE_ENTITY_CALLED);
            assertFalse(Resource.APPLY_SERVER_PARAMS_CALLED);
            assertFalse(Resource.MERGE_CHANGES_CALLED);
            assertFalse(Resource.COMMIT_CALLED);
            assertFalse(Resource.FILL_RESPONSE_CALLED);

            APPLY_SERVER_PARAMS_CALLED = true;
        }

        private void onMergeChanges(UpdateContext<E3> context) {
            assertTrue(Resource.START_CALLED);
            assertTrue(Resource.PARSE_REQUEST_CALLED);
            assertTrue(Resource.CREATE_ENTITY_CALLED);
            assertTrue(Resource.APPLY_SERVER_PARAMS_CALLED);
            assertFalse(Resource.MERGE_CHANGES_CALLED);
            assertFalse(Resource.COMMIT_CALLED);
            assertFalse(Resource.FILL_RESPONSE_CALLED);

            MERGE_CHANGES_CALLED = true;
        }

        private void onCommit(UpdateContext<E3> context) {
            assertTrue(Resource.START_CALLED);
            assertTrue(Resource.PARSE_REQUEST_CALLED);
            assertTrue(Resource.CREATE_ENTITY_CALLED);
            assertTrue(Resource.APPLY_SERVER_PARAMS_CALLED);
            assertTrue(Resource.MERGE_CHANGES_CALLED);
            assertFalse(Resource.COMMIT_CALLED);
            assertFalse(Resource.FILL_RESPONSE_CALLED);

            COMMIT_CALLED = true;
        }

        private void onFillResponse(UpdateContext<E3> context) {

            assertTrue(Resource.START_CALLED);
            assertTrue(Resource.PARSE_REQUEST_CALLED);
            assertTrue(Resource.CREATE_ENTITY_CALLED);
            assertTrue(Resource.APPLY_SERVER_PARAMS_CALLED);
            assertTrue(Resource.MERGE_CHANGES_CALLED);
            assertTrue(Resource.COMMIT_CALLED);
            assertFalse(Resource.FILL_RESPONSE_CALLED);

            FILL_RESPONSE_CALLED = true;
        }
    }
}
