package io.agrest.runtime;

import io.agrest.DataResponse;
import io.agrest.SelectBuilder;
import io.agrest.SelectStage;
import io.agrest.it.fixture.CayenneDerbyStack;
import io.agrest.it.fixture.DbCleaner;
import io.agrest.it.fixture.AgRESTFactory;
import io.agrest.it.fixture.cayenne.E2;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultSelectBuilder_CustomPipeline_DataIT {

    @ClassRule
    public static CayenneDerbyStack DB = new CayenneDerbyStack("DefaultSelectBuilder_CustomPipeline_DataIT");

    @Rule
    public DbCleaner dbCleaner = new DbCleaner(DB.newContext());

    @Rule
    public AgRESTFactory agREST = new AgRESTFactory(DB);

    private <T> DefaultSelectBuilder<T> createBuilder(Class<T> type) {
        SelectBuilder<T> builder = agREST.getAgRESTService().select(type);
        assertTrue(builder instanceof DefaultSelectBuilder);
        return (DefaultSelectBuilder<T>) builder;
    }

    @Test
    public void testStage() {

        DB.insert("e2", "id, name", "1, 'xxx'");
        DB.insert("e2", "id, name", "2, 'yyy'");

        DataResponse<E2> dr = createBuilder(E2.class)
                .stage(SelectStage.CREATE_ENTITY, c -> c.getEntity().setQualifier(E2.NAME.eq("yyy")))
                .get();

        assertEquals(1, dr.getObjects().size());
        assertEquals("yyy", dr.getObjects().get(0).getName());
    }

    @Test
    public void testTerminalStage() {

        DB.insert("e2", "id, name", "1, 'xxx'");

        DataResponse<E2> dr = createBuilder(E2.class)
                .terminalStage(SelectStage.PARSE_REQUEST, c -> {
                })
                .get();

        assertTrue(dr.getObjects().isEmpty());
    }
}
