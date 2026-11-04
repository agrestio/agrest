package io.agrest.cayenne.exp;

import io.agrest.AgException;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.bootique.junit.BQTestTool;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExistsExp;
import org.apache.cayenne.exp.PathExp;
import org.apache.cayenne.exp.SubqueryExp;
import org.apache.cayenne.query.FluentSelect;
import org.apache.cayenne.query.ObjectSelect;
import org.junit.jupiter.api.Test;

import static org.apache.cayenne.exp.ExpressionFactory.*;
import static org.junit.jupiter.api.Assertions.*;

public class ICayenneExpPostProcessorIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester().build();

    ICayenneExpPostProcessor p = tester.runtime().service(ICayenneExpPostProcessor.class);

    @Test
    void process() {
        Expression e0 = exp("name = 'a'");
        Expression e1 = p.process("E2", e0);
        assertEquals(e0, e1);
    }

    @Test
    void id() {
        Expression e0 = exp("id");
        Expression e1 = p.process("E2", e0);
        assertEquals(exp("db:id_"), e1);
    }

    @Test
    void idEq() {
        Expression e0 = exp("id = 4");
        Expression e1 = p.process("E2", e0);
        assertEquals(exp("db:id_ = 4"), e1);
    }

    @Test
    void toOne() {
        Expression e0 = exp("e2");
        Expression e1 = p.process("E3", e0);
        assertEquals(e0, e1);
    }

    @Test
    void toOneEq() {
        Expression e0 = exp("e2 = 2");
        Expression e1 = p.process("E3", e0);
        assertEquals(e0, e1);
    }

    @Test
    void relChain() {
        Expression e0 = exp("e2.e3s");
        Expression e1 = p.process("E3", e0);
        assertEquals(e0, e1);
    }

    @Test
    void relAlias() {
        Expression e0 = exp("e2#alias1");
        Expression e1 = p.process("E3", e0);
        assertEqualsPathAliasAware(e0, e1);
    }

    @Test
    void relChainAlias() {
        Expression e0 = exp("e2#alias1.e3s");
        Expression e1 = p.process("E3", e0);
        assertEqualsPathAliasAware(e0, e1);
    }

    @Test
    void relChainEndAlias() {
        Expression e0 = exp("e2.e3s#alias1");
        Expression e1 = p.process("E3", e0);
        assertEqualsPathAliasAware(e0, e1);
    }

    @Test
    void relChainEndingWithAttribute() {
        Expression e0 = exp("e2.e3s.name");
        Expression e1 = p.process("E3", e0);
        assertEqualsPathAliasAware(e0, e1);
    }

    @Test
    void relChainAliasEndingWithAttribute() {
        Expression e0 = exp("e2#alias1.e3s.name");
        Expression e1 = p.process("E3", e0);
        assertEqualsPathAliasAware(e0, e1);
    }

    @Test
    void relChainAliasEndingWithAttributeExpression() {
        Expression e0 = exp("e2#alias1.e3s.name = 'a' and e2#alias2.e3s.name = 'b'");
        Expression e1 = p.process("E3", e0);
        assertEqualsPathAliasAware(e0, e1);
    }

    @Test
    void relChainId() {
        Expression e0 = exp("e2.e3s.id = 1");
        Expression e1 = p.process("E3", e0);
        assertEquals(e0, e1);
    }

    @Test
    void relChainAliasId() {
        Expression e0 = exp("e2#alias1.e3s.id = 1 and e2#alias2.e3s.id = 2");
        Expression e1 = p.process("E3", e0);
        assertEqualsPathAliasAware(e0, e1);
    }

    @Test
    void toOneId() {
        Expression e0 = exp("e2.id");
        Expression e1 = p.process("E3", e0);
        assertEquals(exp("e2"), e1);
    }

    @Test
    void toOneIdEq() {
        Expression e0 = exp("e2.id = 2");
        Expression e1 = p.process("E3", e0);
        assertEquals(exp("e2 = 2"), e1);
    }

    @Test
    void existsAtRoot() {
        ExistsExp e0 = new ExistsExp(E3.E2.dot(E2.NAME).getExpression());

        Expression e1 = p.process("E3", e0);

        Expression subqueryWhere = E2.NAME.isNotNull()
                .andExp(matchDbExp(E2.ID__PK_COLUMN, enclosingObjectExp(dbPathExp("e2_id"))));
        Expression expected = exists(ObjectSelect.columnQuery(E2.class, E2.NAME).where(subqueryWhere));

        assertEquals(expected, e1);
        // doesn't really compare internals, so need to check it manually
        Object operand = e1.getOperand(0);
        assertInstanceOf(SubqueryExp.class, operand);
        FluentSelect<?, ?> query = ((SubqueryExp) operand).getQuery();
        assertEquals(subqueryWhere, query.getWhere());
    }

    @Test
    void existsAtChild() {
        ExistsExp exists = new ExistsExp(E3.E2.dot(E2.NAME).getExpression());

        Expression e0 = or(exists, E3.PHONE_NUMBER.contains("123"));
        Expression e1 = p.process("E3", e0);

        Expression subqueryWhere = E2.NAME.isNotNull()
                .andExp(matchDbExp(E2.ID__PK_COLUMN, enclosingObjectExp(dbPathExp("e2_id"))));
        Expression expected = or(
                exists(ObjectSelect.columnQuery(E2.class, E2.NAME).where(subqueryWhere)),
                E3.PHONE_NUMBER.contains("123")
        );

        assertEquals(expected, e1);
        // doesn't really compare internals, so need to check it manually
        Object operand = e1.getOperand(0);
        assertInstanceOf(ExistsExp.class, operand);
        Object queryOperand = ((ExistsExp) operand).getOperand(0);
        assertInstanceOf(SubqueryExp.class, queryOperand);
        FluentSelect<?, ?> query = ((SubqueryExp) queryOperand).getQuery();
        assertEquals(subqueryWhere, query.getWhere());
    }

    @Test
    void invalidPath() {
        assertThrows(AgException.class, () -> p.process("E2", exp("a.b.c = 2")));
    }

    static void assertEqualsPathAliasAware(Expression expected, Expression actual) {
        assertEquals(expected, actual);

        if (expected instanceof PathExp expectedPath && actual instanceof PathExp actualPath) {
            assertEquals(expectedPath.getPathAliases(), actualPath.getPathAliases(), "Expressions have different path aliases");
        }

        for (int i = 0; i < expected.getOperandCount(); i++) {
            if (expected.getOperand(i) instanceof Expression expectedChild
                    && actual.getOperand(i) instanceof Expression actualChild) {
                assertEqualsPathAliasAware(expectedChild, actualChild);
            }
        }
    }
}
