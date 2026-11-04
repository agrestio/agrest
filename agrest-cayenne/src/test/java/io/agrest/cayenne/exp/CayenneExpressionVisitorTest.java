package io.agrest.cayenne.exp;

import io.agrest.protocol.Exp;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.PatternMatchExp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;


public class CayenneExpressionVisitorTest {

    static final CayenneExpressionVisitor visitor = new CayenneExpressionVisitor();

    @ParameterizedTest
    @CsvSource(delimiterString = "_|", value = {
            "abs(1)_|org.apache.cayenne.exp.AbsExp",
            "1 + 2_|org.apache.cayenne.exp.AddExp",
            "t.isA = true and t.isB = true_|org.apache.cayenne.exp.AndExp",
            "t.value between 10 and 20_|org.apache.cayenne.exp.BetweenExp",
            "0xFF & 0x01_|org.apache.cayenne.exp.BitwiseAndExp",
            "0xFF << 2_|org.apache.cayenne.exp.BitwiseLeftShiftExp",
            "~0xA7_|org.apache.cayenne.exp.BitwiseNotExp",
            "0xFF | 0x01_|org.apache.cayenne.exp.BitwiseOrExp",
            "0xFF >> 2_|org.apache.cayenne.exp.BitwiseRightShiftExp",
            "0xFF ^ 0x01_|org.apache.cayenne.exp.BitwiseXorExp",
            "concat(t.v, '10')_|org.apache.cayenne.exp.ConcatExp",
            "currentDate()_|org.apache.cayenne.exp.CurrentDateExp",
            "currentTime()_|org.apache.cayenne.exp.CurrentTimeExp",
            "currentTimestamp()_|org.apache.cayenne.exp.CurrentTimestampExp",
            "t.value / 2_|org.apache.cayenne.exp.DivideExp",
            "t.v1 = t.v2_|org.apache.cayenne.exp.EqualExp",
            "exists details_|org.apache.cayenne.exp.ExistsExp",
            "day(t.dateTime)_|org.apache.cayenne.exp.ExtractExp",
            "false_|org.apache.cayenne.exp.FalseExp",
            "t.v > 0_|org.apache.cayenne.exp.GreaterExp",
            "t.v >= 0_|org.apache.cayenne.exp.GreaterOrEqualExp",
            "t.v in (0, 5)_|org.apache.cayenne.exp.InExp",
            "length(a.v)_|org.apache.cayenne.exp.LengthExp",
            "t.v < 0_|org.apache.cayenne.exp.LessExp",
            "t.v <= 0_|org.apache.cayenne.exp.LessOrEqualExp",
            "t.name like '%s'_|org.apache.cayenne.exp.LikeExp",
            "t.name likeIgnoreCase '%s'_|org.apache.cayenne.exp.LikeIgnoreCaseExp",
            "locate(t.v, 'id')_|org.apache.cayenne.exp.LocateExp",
            "lower(t.v)_|org.apache.cayenne.exp.LowerExp",
            "mod(t.v, 10)_|org.apache.cayenne.exp.ModExp",
            "1 * 4_|org.apache.cayenne.exp.MultiplyExp",
            "$a_|org.apache.cayenne.exp.ScalarExp",
            "-a.v_|org.apache.cayenne.exp.NegateExp",
            "!(t.a = 1 and t.b = 3)_|org.apache.cayenne.exp.NotExp",
            "t.value !between 10 and 20_|org.apache.cayenne.exp.NotBetweenExp",
            "t.v1 != t.v2_|org.apache.cayenne.exp.NotEqualExp",
            "not exists details_|org.apache.cayenne.exp.NotExistsExp",
            "t.v !in (0, 5)_|org.apache.cayenne.exp.NotInExp",
            "t.name !like '%s'_|org.apache.cayenne.exp.NotLikeExp",
            "t.name !likeIgnoreCase '%s'_|org.apache.cayenne.exp.NotLikeIgnoreCaseExp",
            "a.v_|org.apache.cayenne.exp.ObjPathExp",
            "t.isA = true or t.isB = true_|org.apache.cayenne.exp.OrExp",
            "1.2_|org.apache.cayenne.exp.ScalarExp",
            "null_|org.apache.cayenne.exp.ScalarExp",
            "1_|org.apache.cayenne.exp.ScalarExp",
            "\"value\"_|org.apache.cayenne.exp.ScalarExp",
            "sqrt(2)_|org.apache.cayenne.exp.SqrtExp",
            "substring(a.v, 3)_|org.apache.cayenne.exp.SubstringExp",
            "3 - 1_|org.apache.cayenne.exp.SubtractExp",
            "trim(a.v)_|org.apache.cayenne.exp.TrimExp",
            "true_|org.apache.cayenne.exp.TrueExp",
            "upper(t.v)_|org.apache.cayenne.exp.UpperExp"

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
        assertInstanceOf(PatternMatchExp.class, cayenneExp);
        PatternMatchExp matchNode = (PatternMatchExp) cayenneExp;
        assertEquals('$', matchNode.getEscapeChar());
    }
}