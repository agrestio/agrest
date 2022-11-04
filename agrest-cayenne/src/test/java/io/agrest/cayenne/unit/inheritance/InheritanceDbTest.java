package io.agrest.cayenne.unit.inheritance;

import io.agrest.cayenne.unit.AgCayenneTester;
import io.bootique.jdbc.junit5.derby.DerbyTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.BQTestTool;

/**
 * An abstract superclass of integration tests that starts Bootique test runtime with JAX-RS service and Derby DB.
 */
@BQTest
public abstract class InheritanceDbTest {

    @BQTestTool(BQTestScope.GLOBAL)
    static final DerbyTester db = DerbyTester.db().initDB("classpath:inheritance/schema-derby.sql");

    protected static AgCayenneTester.Builder<InheritanceModelTester> tester(Class<?>... resources) {
        return new Builder()
                .db(db)
                .cayenneProject("inheritance/cayenne-project.xml")
                .resources(resources);
    }

    public static class Builder extends AgCayenneTester.Builder<InheritanceModelTester> {
        public Builder() {
            super(new InheritanceModelTester());
        }
    }
}
