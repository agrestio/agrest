package io.agrest.cayenne.exp;

import io.agrest.protocol.Exp;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.PatternMatchNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;


public class CayenneExpressionVisitorTest {

    static final CayenneExpressionVisitor visitor = new CayenneExpressionVisitor();

    @ParameterizedTest
    @CsvSource(delimiterString = "_|", value = {
            "abs(1)_|org.apache.cayenne.exp.parser.ASTAbs",
            "1 + 2_|org.apache.cayenne.exp.parser.ASTAdd",
            "t.isA = true and t.isB = true_|org.apache.cayenne.exp.parser.ASTAnd",
            "t.value between 10 and 20_|org.apache.cayenne.exp.parser.ASTBetween",
            "0xFF & 0x01_|org.apache.cayenne.exp.parser.ASTBitwiseAnd",
            "0xFF << 2_|org.apache.cayenne.exp.parser.ASTBitwiseLeftShift",
            "~0xA7_|org.apache.cayenne.exp.parser.ASTBitwiseNot",
            "0xFF | 0x01_|org.apache.cayenne.exp.parser.ASTBitwiseOr",
            "0xFF >> 2_|org.apache.cayenne.exp.parser.ASTBitwiseRightShift",
            "0xFF ^ 0x01_|org.apache.cayenne.exp.parser.ASTBitwiseXor",
            "concat(t.v, '10')_|org.apache.cayenne.exp.parser.ASTConcat",
            "currentDate()_|org.apache.cayenne.exp.parser.ASTCurrentDate",
            "currentTime()_|org.apache.cayenne.exp.parser.ASTCurrentTime",
            "currentTimestamp()_|org.apache.cayenne.exp.parser.ASTCurrentTimestamp",
            "t.value / 2_|org.apache.cayenne.exp.parser.ASTDivide",
            "t.v1 = t.v2_|org.apache.cayenne.exp.parser.ASTEqual",
            "exists details_|org.apache.cayenne.exp.parser.ASTExists",
            "day(t.dateTime)_|org.apache.cayenne.exp.parser.ASTExtract",
            "false_|org.apache.cayenne.exp.parser.ASTFalse",
            "t.v > 0_|org.apache.cayenne.exp.parser.ASTGreater",
            "t.v >= 0_|org.apache.cayenne.exp.parser.ASTGreaterOrEqual",
            "t.v in (0, 5)_|org.apache.cayenne.exp.parser.ASTIn",
            "length(a.v)_|org.apache.cayenne.exp.parser.ASTLength",
            "t.v < 0_|org.apache.cayenne.exp.parser.ASTLess",
            "t.v <= 0_|org.apache.cayenne.exp.parser.ASTLessOrEqual",
            "t.name like '%s'_|org.apache.cayenne.exp.parser.ASTLike",
            "t.name likeIgnoreCase '%s'_|org.apache.cayenne.exp.parser.ASTLikeIgnoreCase",
            "locate(t.v, 'id')_|org.apache.cayenne.exp.parser.ASTLocate",
            "lower(t.v)_|org.apache.cayenne.exp.parser.ASTLower",
            "mod(t.v, 10)_|org.apache.cayenne.exp.parser.ASTMod",
            "1 * 4_|org.apache.cayenne.exp.parser.ASTMultiply",
            "$a_|org.apache.cayenne.exp.parser.ASTNamedParameter",
            "-a.v_|org.apache.cayenne.exp.parser.ASTNegate",
            "!(t.a = 1 and t.b = 3)_|org.apache.cayenne.exp.parser.ASTNot",
            "t.value !between 10 and 20_|org.apache.cayenne.exp.parser.ASTNotBetween",
            "t.v1 != t.v2_|org.apache.cayenne.exp.parser.ASTNotEqual",
            "not exists details_|org.apache.cayenne.exp.parser.ASTNotExists",
            "t.v !in (0, 5)_|org.apache.cayenne.exp.parser.ASTNotIn",
            "t.name !like '%s'_|org.apache.cayenne.exp.parser.ASTNotLike",
            "t.name !likeIgnoreCase '%s'_|org.apache.cayenne.exp.parser.ASTNotLikeIgnoreCase",
            "a.v_|org.apache.cayenne.exp.parser.ASTObjPath",
            "t.isA = true or t.isB = true_|org.apache.cayenne.exp.parser.ASTOr",
            "1.2_|org.apache.cayenne.exp.parser.ASTScalar",
            "null_|org.apache.cayenne.exp.parser.ASTScalar",
            "1_|org.apache.cayenne.exp.parser.ASTScalar",
            "\"value\"_|org.apache.cayenne.exp.parser.ASTScalar",
            "sqrt(2)_|org.apache.cayenne.exp.parser.ASTSqrt",
            "substring(a.v, 3)_|org.apache.cayenne.exp.parser.ASTSubstring",
            "3 - 1_|org.apache.cayenne.exp.parser.ASTSubtract",
            "trim(a.v)_|org.apache.cayenne.exp.parser.ASTTrim",
            "true_|org.apache.cayenne.exp.parser.ASTTrue",
            "upper(t.v)_|org.apache.cayenne.exp.parser.ASTUpper"

            // TODO: Cayenne doesn't allow objPath as operand for logical operators (see AggregateConditionNode).
            //       It will be reasonable to change this.
            //"!t.m", ASTNot
            //"t.isA and t.isB ", ASTAnd
            //"t.isA or t.isB ", ASTOr
    })
    public void accept_ReturnedType(String agrestExp, Class<? extends Expression> cayenneExpExpectedType) {
        Expression cayenneExp = Exp.parse(agrestExp).accept(visitor, null);
        assertEquals(cayenneExpExpectedType, cayenneExp.getClass());
    }

    @ParameterizedTest(name = "case {index}")
    @ValueSource(strings = {
            "a like 'bcd' escape '$'",
            "a likeIgnoreCase 'bcd' escape '$'",
            "a not like 'bcd' escape '$'",
            "a not likeIgnoreCase 'bcd' escape '$'"})
    public void accept_escapeChar(String agrestExp) {
        Expression cayenneExp = Exp.parse(agrestExp).accept(visitor, null);
        assertInstanceOf(PatternMatchNode.class, cayenneExp);
        PatternMatchNode matchNode = (PatternMatchNode) cayenneExp;
        assertEquals('$', matchNode.getEscapeChar());
    }
}