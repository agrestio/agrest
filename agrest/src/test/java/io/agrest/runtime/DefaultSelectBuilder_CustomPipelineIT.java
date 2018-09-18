package io.agrest.runtime;

import io.agrest.DataResponse;
import io.agrest.SelectBuilder;
import io.agrest.SelectStage;
import io.agrest.it.fixture.CayenneDerbyStack;
import io.agrest.it.fixture.AgFactory;
import io.agrest.it.fixture.cayenne.E2;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * The test class for operations that execute and verify the callbacks and custom stack functions, but do not check
 * the data. So no need to run DB cleanup.
 */
public class DefaultSelectBuilder_CustomPipelineIT {

    @ClassRule
    public static CayenneDerbyStack DB = new CayenneDerbyStack("DefaultSelectBuilder_CustomPipelineIT");

    @Rule
    public AgFactory agREST = new AgFactory(DB);

    private <T> DefaultSelectBuilder<T> createBuilder(Class<T> type) {
        SelectBuilder<T> builder = agREST.getService().select(type);
        assertTrue(builder instanceof DefaultSelectBuilder);
        return (DefaultSelectBuilder<T>) builder;
    }

    @Test
    public void testStage_AllStages() {

        Map<SelectStage, Integer> stages = new EnumMap<>(SelectStage.class);
        Consumer<SelectStage> stageRecorder = s -> stages.put(s, stages.size());

        DataResponse<E2> dr = createBuilder(E2.class)
                // Order of registration across stages is not significant. Stage natural order will be preserved.
                .stage(SelectStage.PARSE_REQUEST, c -> stageRecorder.accept(SelectStage.PARSE_REQUEST))
                .stage(SelectStage.CREATE_ENTITY, c -> stageRecorder.accept(SelectStage.CREATE_ENTITY))
                .stage(SelectStage.START, c -> stageRecorder.accept(SelectStage.START))
                .stage(SelectStage.FETCH_DATA, c -> stageRecorder.accept(SelectStage.FETCH_DATA))
                .stage(SelectStage.ASSEMBLE_QUERY, c -> stageRecorder.accept(SelectStage.ASSEMBLE_QUERY))
                .stage(SelectStage.APPLY_SERVER_PARAMS, c -> stageRecorder.accept(SelectStage.APPLY_SERVER_PARAMS))
                .get();

        assertEquals(SelectStage.values().length, stages.size());
        stages.forEach((k, v) -> assertEquals(k.ordinal(), v.intValue()));
    }

    @Test
    public void testStage_Composition() {

        Map<SelectStage, String> stages = new EnumMap<>(SelectStage.class);
        BiConsumer<SelectStage, String> stageRecorder = (stage, value) -> {
            String existing = stages.computeIfAbsent(stage, a -> "");
            stages.put(stage, existing + "_" + value);
        };

        DataResponse<E2> dr = createBuilder(E2.class)
                // Order of registration with stages is significant.
                .stage(SelectStage.PARSE_REQUEST, c -> stageRecorder.accept(SelectStage.PARSE_REQUEST, "a"))
                .stage(SelectStage.FETCH_DATA, c -> stageRecorder.accept(SelectStage.FETCH_DATA, "b"))
                .stage(SelectStage.PARSE_REQUEST, c -> stageRecorder.accept(SelectStage.PARSE_REQUEST, "c"))
                .stage(SelectStage.PARSE_REQUEST, c -> stageRecorder.accept(SelectStage.PARSE_REQUEST, "d"))
                .stage(SelectStage.FETCH_DATA, c -> stageRecorder.accept(SelectStage.FETCH_DATA, "e"))
                .get();

        assertEquals(2, stages.size());
        assertEquals("_a_c_d", stages.get(SelectStage.PARSE_REQUEST));
        assertEquals("_b_e", stages.get(SelectStage.FETCH_DATA));
    }
}
