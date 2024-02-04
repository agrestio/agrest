package io.agrest.cayenne.unit.inheritance;

import io.agrest.cayenne.unit.NoDbTest;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * A superclass of Cayenne-aware test cases that do not need to access the DB, but need to work with EntityResolver
 * and higher levels of the stack.
 */
public abstract class InheritanceNoDbTest extends NoDbTest {

    protected static ServerRuntime runtime;

    @BeforeAll
    public static void setUpClass() {
        runtime = createRuntime("inheritance/cayenne-project.xml");
    }

    @AfterAll
    public static void tearDownClass() {
        runtime.shutdown();
        runtime = null;
    }

    @Override
    public ServerRuntime getRuntime() {
        return runtime;
    }
}
