package io.agrest.cayenne.exp;

import io.agrest.AgException;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.access.translator.select.TranslatableQueryWrapper;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.ASTExists;
import org.apache.cayenne.exp.parser.ASTSubquery;
import org.apache.cayenne.exp.parser.SimpleNode;
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
    void relChainEndingWithAttribute() {
        Expression e0 = exp("e2.e3s.name");
        Expression e1 = p.process("E3", e0);
        assertEquals(e0, e1);
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
        // can't create this via API, so it's ugly
        ASTExists e0 = new ASTExists(null);
        e0.jjtAddChild((SimpleNode)E3.E2.dot(E2.NAME).getExpression(), 0);

        Expression e1 = p.process("E3", e0);

        Expression subqueryWhere = E2.NAME.isNotNull()
                .andExp(matchDbExp(E2.ID__PK_COLUMN, enclosingObjectExp(dbPathExp("e2_id"))));
        Expression expected = exists(ObjectSelect.columnQuery(E2.class, E2.NAME).where(subqueryWhere));

        assertEquals(expected, e1);
        // doesn't really compare internals, so need to check it manually
        Object operand = e1.getOperand(0);
        assertInstanceOf(ASTSubquery.class, operand);
        TranslatableQueryWrapper query = ((ASTSubquery) operand).getQuery();
        assertEquals(subqueryWhere, query.getQualifier());
    }

    @Test
    void existsAtChild() {
        // can't create this via API, so it's ugly
        ASTExists exists = new ASTExists(null);
        exists.jjtAddChild((SimpleNode)E3.E2.dot(E2.NAME).getExpression(), 0);

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
        assertInstanceOf(ASTExists.class, operand);
        Object queryOperand = ((ASTExists) operand).getOperand(0);
        assertInstanceOf(ASTSubquery.class, queryOperand);
        TranslatableQueryWrapper query = ((ASTSubquery) queryOperand).getQuery();
        assertEquals(subqueryWhere, query.getQualifier());
    }

    @Test
    void invalidPath() {
        assertThrows(AgException.class, () -> p.process("E2", exp("a.b.c = 2")));
    }
}
