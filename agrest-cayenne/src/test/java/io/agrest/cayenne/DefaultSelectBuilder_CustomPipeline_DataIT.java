package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.SelectBuilder;
import io.agrest.SelectStage;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.runtime.DefaultSelectBuilder;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultSelectBuilder_CustomPipeline_DataIT extends DbTest {

    @BQTestTool
    static final CayenneAgTester tester = tester()

            .entities(E2.class, E3.class)
            .build();

    private <T> DefaultSelectBuilder<T> createBuilder(Class<T> type) {
        SelectBuilder<T> builder = tester.ag().select(type);
        assertTrue(builder instanceof DefaultSelectBuilder);
        return (DefaultSelectBuilder<T>) builder;
    }

    @Test
    public void testStage() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        DataResponse<E2> dr = createBuilder(E2.class)
                .stage(SelectStage.CREATE_ENTITY, c -> c.getEntity().setQualifier(E2.NAME.eq("yyy")))
                .get();

        assertEquals(1, dr.getObjects().size());
        assertEquals("yyy", dr.getObjects().get(0).getName());
    }

    @Test
    public void testTerminalStage() {

        tester.e2().insertColumns("id_", "name").values(1, "xxx").exec();

        DataResponse<E2> dr = createBuilder(E2.class)
                .terminalStage(SelectStage.PARSE_REQUEST, c -> {
                })
                .get();

        assertTrue(dr.getObjects().isEmpty());
    }
}
