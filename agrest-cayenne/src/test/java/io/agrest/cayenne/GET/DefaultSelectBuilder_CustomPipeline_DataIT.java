package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.SelectBuilder;
import io.agrest.SelectStage;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.protocol.Exp;
import io.agrest.runtime.DefaultSelectBuilder;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultSelectBuilder_CustomPipeline_DataIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester()
            .entities(E2.class, E3.class)
            .build();

    private <T> DefaultSelectBuilder<T> createBuilder(Class<T> type) {
        SelectBuilder<T> builder = tester.runtime().select(type);
        assertTrue(builder instanceof DefaultSelectBuilder);
        return (DefaultSelectBuilder<T>) builder;
    }

    @Test
    public void stage() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        DataResponse<E2> dr = createBuilder(E2.class)
                .stage(SelectStage.CREATE_ENTITY, c -> c.getEntity().andExp(Exp.from("name = 'yyy'")))
                .get();

        assertEquals(1, dr.getData().size());
        assertEquals("yyy", dr.getData().get(0).getName());
    }

    @Test
    public void terminalStage() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();

        DataResponse<E2> dr = createBuilder(E2.class)
                .terminalStage(SelectStage.APPLY_SERVER_PARAMS, c -> {
                })
                .get();

        assertTrue(dr.getData().isEmpty());
    }
}
