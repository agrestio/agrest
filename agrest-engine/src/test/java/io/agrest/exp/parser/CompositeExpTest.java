package io.agrest.exp.parser;

import org.junit.jupiter.api.Test;

import static io.agrest.exp.parser.ExpBuilder.expFromType;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompositeExpTest {

    @Test
    public void absSum() {
        SimpleNode expected = expFromType(ExpAbs.class)
                .addChild(expFromType(ExpAdd.class)
                        .addChild(expFromType(ExpScalar.class)
                                .withValue(1))
                        .addChild(expFromType(ExpScalar.class)
                                .withValue(2))).build();

        assertEquals(expected, AgExpressionParser.parse("abs(1 + 2)"));
    }

    @Test
    public void multiplyAdd() {
        SimpleNode expected = expFromType(ExpAdd.class)
                .addChild(expFromType(ExpMultiply.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("a"))
                        .addChild(expFromType(ExpPath.class)
                                .withValue("b")))
                .addChild(expFromType(ExpPath.class)
                        .withValue("c"))
                .build();

        assertEquals(expected, AgExpressionParser.parse("a * b + c"));
    }

    @Test
    public void multiplyAdd_Grouped() {
        SimpleNode expected = expFromType(ExpMultiply.class)
                .addChild(expFromType(ExpPath.class)
                        .withValue("a"))
                .addChild(expFromType(ExpAdd.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("b"))
                        .addChild(expFromType(ExpPath.class)
                                .withValue("c")))
                .build();

        assertEquals(expected, AgExpressionParser.parse("a * (b + c)"));
    }

    @Test
    public void divideAdd() {
        SimpleNode expected = expFromType(ExpAdd.class)
                .addChild(expFromType(ExpDivide.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("a"))
                        .addChild(expFromType(ExpPath.class)
                                .withValue("b")))
                .addChild(expFromType(ExpPath.class)
                        .withValue("c"))
                .build();

        assertEquals(expected, AgExpressionParser.parse("a / b + c"));
    }

    @Test
    public void divideAdd_Grouped() {
        SimpleNode expected = expFromType(ExpDivide.class)
                .addChild(expFromType(ExpPath.class)
                        .withValue("a"))
                .addChild(expFromType(ExpAdd.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("b"))
                        .addChild(expFromType(ExpPath.class)
                                .withValue("c")))
                .build();

        assertEquals(expected, AgExpressionParser.parse("a / (b + c)"));
    }

    @Test
    public void bitwiseAndOr() {
        SimpleNode expected = expFromType(ExpBitwiseOr.class)
                .addChild(expFromType(ExpBitwiseAnd.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("a"))
                        .addChild(expFromType(ExpPath.class)
                                .withValue("b")))
                .addChild(expFromType(ExpPath.class)
                        .withValue("c"))
                .build();

        assertEquals(expected, AgExpressionParser.parse("a & b | c"));
    }

    @Test
    public void bitwiseAndOr_Grouped() {
        SimpleNode expected = expFromType(ExpBitwiseAnd.class)
                .addChild(expFromType(ExpPath.class)
                        .withValue("a"))
                .addChild(expFromType(ExpBitwiseOr.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("b"))
                        .addChild(expFromType(ExpPath.class)
                                .withValue("c")))
                .build();

        assertEquals(expected, AgExpressionParser.parse("a & (b | c)"));
    }

    @Test
    public void bitwiseAndXor() {
        SimpleNode expected = expFromType(ExpBitwiseXor.class)
                .addChild(expFromType(ExpBitwiseAnd.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("a"))
                        .addChild(expFromType(ExpPath.class)
                                .withValue("b")))
                .addChild(expFromType(ExpPath.class)
                        .withValue("c"))
                .build();

        assertEquals(expected, AgExpressionParser.parse("a & b ^ c"));
    }

    @Test
    public void bitwiseAndXor_Grouped() {
        SimpleNode expected = expFromType(ExpBitwiseAnd.class)
                .addChild(expFromType(ExpPath.class)
                        .withValue("a"))
                .addChild(expFromType(ExpBitwiseXor.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("b"))
                        .addChild(expFromType(ExpPath.class)
                                .withValue("c")))
                .build();

        assertEquals(expected, AgExpressionParser.parse("a & (b ^ c)"));
    }

    @Test
    public void shiftAnd() {
        SimpleNode expected = expFromType(ExpBitwiseAnd.class)
                .addChild(expFromType(ExpBitwiseLeftShift.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("a"))
                        .addChild(expFromType(ExpPath.class)
                                .withValue("b")))
                .addChild(expFromType(ExpPath.class)
                        .withValue("c"))
                .build();

        assertEquals(expected, AgExpressionParser.parse("a << b & c"));
    }

    @Test
    public void shiftAnd_Grouped() {
        SimpleNode expected = expFromType(ExpBitwiseLeftShift.class)
                .addChild(expFromType(ExpPath.class)
                        .withValue("a"))
                .addChild(expFromType(ExpBitwiseAnd.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("b"))
                        .addChild(expFromType(ExpPath.class)
                                .withValue("c")))
                .build();

        assertEquals(expected, AgExpressionParser.parse("a << (b & c)"));
    }

    @Test
    public void andOr() {
        SimpleNode expected = expFromType(ExpOr.class)
                .addChild(expFromType(ExpAnd.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("a"))
                        .addChild(expFromType(ExpPath.class)
                                .withValue("b")))
                .addChild(expFromType(ExpPath.class)
                        .withValue("c"))
                .build();

        assertEquals(expected, AgExpressionParser.parse("a and b or c"));
    }

    @Test
    public void andOr_Grouped() {
        SimpleNode expected = expFromType(ExpAnd.class)
                .addChild(expFromType(ExpPath.class)
                        .withValue("a"))
                .addChild(expFromType(ExpOr.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("b"))
                        .addChild(expFromType(ExpPath.class)
                                .withValue("c")))
                .build();

        assertEquals(expected, AgExpressionParser.parse("a and (b or c)"));
    }

    @Test
    public void greaterAndLike() {
        SimpleNode expected = expFromType(ExpAnd.class)
                .addChild(expFromType(ExpGreater.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("t.amount"))
                        .addChild(expFromType(ExpScalar.class)
                                .withValue(0)))
                .addChild(expFromType(ExpLike.class)
                        .addChild(expFromType(ExpPath.class)
                                .withValue("t.name"))
                        .addChild(expFromType(ExpScalar.class)
                                .withValue("'%story'")))
                .build();

        assertEquals(expected, AgExpressionParser.parse("t.amount > 0 and t.name like '%story'"));
    }
}
