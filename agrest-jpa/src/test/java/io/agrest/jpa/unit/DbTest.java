package io.agrest.jpa.unit;

import io.bootique.jdbc.junit5.derby.DerbyTester;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.BQTestTool;

@BQTest
public class DbTest {

    @BQTestTool(BQTestScope.GLOBAL)
    protected static final DerbyTester db = DerbyTester.db().initDB("classpath:schema-derby.sql");

    protected static AgJpaTester.Builder tester(Class<?>... resources) {
        return AgJpaTester
                .forDb(db)
//                .cayenneProject("cayenne-project.xml")
                .resources(resources);
    }


}
