package io.agrest.cayenne.GET;

import io.agrest.SelectBuilder;
import io.agrest.SelectStage;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.runtime.DefaultSelectBuilder;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The test class for operations that execute and verify the callbacks and custom stack functions, but do not check
 * the data. So no need to run DB cleanup.
 */
public class DefaultSelectBuilder_CustomPipelineIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester().build();

    private <T> DefaultSelectBuilder<T> createBuilder(Class<T> type) {
        SelectBuilder<T> builder = tester.runtime().select(type);
        assertTrue(builder instanceof DefaultSelectBuilder);
        return (DefaultSelectBuilder<T>) builder;
    }

    @Test
    public void stage_AllStages() {

        Map<SelectStage, Integer> stages = new EnumMap<>(SelectStage.class);
        Consumer<SelectStage> stageRecorder = s -> stages.put(s, stages.size());

        createBuilder(E2.class)
                // Order of registration across stages is not significant. Stage natural order will be preserved.
                .stage(SelectStage.CREATE_ENTITY, c -> stageRecorder.accept(SelectStage.CREATE_ENTITY))
                .stage(SelectStage.START, c -> stageRecorder.accept(SelectStage.START))
                .stage(SelectStage.FETCH_DATA, c -> stageRecorder.accept(SelectStage.FETCH_DATA))
                .stage(SelectStage.FILTER_RESULT, c -> stageRecorder.accept(SelectStage.FILTER_RESULT))
                .stage(SelectStage.ENCODE, c -> stageRecorder.accept(SelectStage.ENCODE))
                .stage(SelectStage.ASSEMBLE_QUERY, c -> stageRecorder.accept(SelectStage.ASSEMBLE_QUERY))
                .stage(SelectStage.APPLY_SERVER_PARAMS, c -> stageRecorder.accept(SelectStage.APPLY_SERVER_PARAMS))
                .get();

        assertEquals(SelectStage.values().length, stages.size());
        stages.forEach((k, v) -> assertEquals(k.ordinal(), v.intValue()));
    }

    @Test
    public void stage_Composition() {

        Map<SelectStage, String> stages = new EnumMap<>(SelectStage.class);
        BiConsumer<SelectStage, String> stageRecorder = (stage, value) -> {
            String existing = stages.computeIfAbsent(stage, a -> "");
            stages.put(stage, existing + "_" + value);
        };

        createBuilder(E2.class)
                // Order of registration with stages is significant.
                .stage(SelectStage.APPLY_SERVER_PARAMS, c -> stageRecorder.accept(SelectStage.APPLY_SERVER_PARAMS, "a"))
                .stage(SelectStage.FETCH_DATA, c -> stageRecorder.accept(SelectStage.FETCH_DATA, "b"))
                .stage(SelectStage.APPLY_SERVER_PARAMS, c -> stageRecorder.accept(SelectStage.APPLY_SERVER_PARAMS, "c"))
                .stage(SelectStage.APPLY_SERVER_PARAMS, c -> stageRecorder.accept(SelectStage.APPLY_SERVER_PARAMS, "d"))
                .stage(SelectStage.FETCH_DATA, c -> stageRecorder.accept(SelectStage.FETCH_DATA, "e"))
                .stage(SelectStage.ENCODE, c -> stageRecorder.accept(SelectStage.ENCODE, "f"))
                .get();

        assertEquals(3, stages.size());
        assertEquals("_a_c_d", stages.get(SelectStage.APPLY_SERVER_PARAMS));
        assertEquals("_b_e", stages.get(SelectStage.FETCH_DATA));
        assertEquals("_f", stages.get(SelectStage.ENCODE));
    }
}
