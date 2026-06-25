package io.agrest.cayenne.unit.main;

import io.agrest.cayenne.unit.NoDbTest;
import org.apache.cayenne.runtime.CayenneRuntime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * A superclass of Cayenne-aware test cases that do not need to access the DB, but need to work with EntityResolver
 * and higher levels of the stack.
 */
public abstract class MainNoDbTest extends NoDbTest {

    protected static CayenneRuntime runtime;

    @BeforeAll
    public static void setUpClass() {
        runtime = createRuntime("main/cayenne-project.xml");
    }

    @AfterAll
    public static void tearDownClass() {
        runtime.shutdown();
        runtime = null;
    }

    @Override
    public CayenneRuntime getRuntime() {
        return runtime;
    }
}
