package io.agrest.cayenne.unit.inheritance;


import io.agrest.cayenne.unit.AgCayenneTester;
import io.bootique.jdbc.junit5.Table;

public class InheritanceModelTester extends AgCayenneTester {

    public Table ie1() {
        return db.getTable("ie1");
    }

    public Table ie2() {
        return db.getTable("ie2");
    }

    public Table ie3() {
        return db.getTable("ie3");
    }

}
