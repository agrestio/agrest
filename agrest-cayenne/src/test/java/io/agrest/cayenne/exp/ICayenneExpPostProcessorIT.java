package io.agrest.cayenne.exp;

import io.agrest.AgException;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ICayenneExpPostProcessorIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester().build();

    ICayenneExpPostProcessor p = tester.runtime().service(ICayenneExpPostProcessor.class);

    @Test
    void process() {
        Expression e0 = ExpressionFactory.exp("name = 'a'");
        Expression e1 = p.process("E2", e0);
        assertEquals(e0, e1);
    }

    @Test
    void id() {
        Expression e0 = ExpressionFactory.exp("id");
        Expression e1 = p.process("E2", e0);
        assertEquals(ExpressionFactory.exp("db:id_"), e1);
    }

    @Test
    void idEq() {
        Expression e0 = ExpressionFactory.exp("id = 4");
        Expression e1 = p.process("E2", e0);
        assertEquals(ExpressionFactory.exp("db:id_ = 4"), e1);
    }

    @Test
    void toOne() {
        Expression e0 = ExpressionFactory.exp("e2");
        Expression e1 = p.process("E3", e0);
        assertEquals(e0, e1);
    }

    @Test
    void toOneEq() {
        Expression e0 = ExpressionFactory.exp("e2 = 2");
        Expression e1 = p.process("E3", e0);
        assertEquals(e0, e1);
    }

    @Test
    void relChain() {
        Expression e0 = ExpressionFactory.exp("e2.e3s");
        Expression e1 = p.process("E3", e0);
        assertEquals(e0, e1);
    }

    @Test
    void relChainEndingWithAttribute() {
        Expression e0 = ExpressionFactory.exp("e2.e3s.name");
        Expression e1 = p.process("E3", e0);
        assertEquals(e0, e1);
    }

    @Test
    void toOneId() {
        Expression e0 = ExpressionFactory.exp("e2.id");
        Expression e1 = p.process("E3", e0);
        assertEquals(ExpressionFactory.exp("e2"), e1);
    }

    @Test
    void toOneIdEq() {
        Expression e0 = ExpressionFactory.exp("e2.id = 2");
        Expression e1 = p.process("E3", e0);
        assertEquals(ExpressionFactory.exp("e2 = 2"), e1);
    }

    @Test
    void invalidPath() {
        assertThrows(AgException.class, () -> p.process("E2", ExpressionFactory.exp("a.b.c = 2")));
    }
}
