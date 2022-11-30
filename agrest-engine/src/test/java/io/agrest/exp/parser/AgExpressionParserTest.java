package io.agrest.exp.parser;

import io.agrest.protocol.Exp;
import org.junit.jupiter.api.Test;

import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class AgExpressionParserTest {

    @Test
    public void abs() {
        Exp abs = AgExpressionParser.parse("abs(a)");
        assertExp(abs, new ExpTestVisitor(){
            @Override
            public BooleanSupplier visit(ExpAbs node, BooleanSupplier data) {
                return () -> true;
            }
        });
    }

    @Test
    public void add() {
        Exp add = AgExpressionParser.parse("a + b");
        assertExp(add, new ExpTestVisitor(){
            @Override
            public BooleanSupplier visit(ExpAdd node, BooleanSupplier data) {
                return () -> true;
            }
        });
    }

    @Test
    public void and() {
        Exp and = AgExpressionParser.parse("a and b and c");
        assertExp(and, new ExpTestVisitor(){
            @Override
            public BooleanSupplier visit(ExpAnd node, BooleanSupplier data) {
                return () -> true;
            }
        });
    }

    private static void assertExp(Exp abs, AgExpressionParserDefaultVisitor<BooleanSupplier> visitor) {
        assertNotNull(abs);
        assertNotNull(visitor);
        assertTrue(abs.accept(visitor, () -> false));
    }

}