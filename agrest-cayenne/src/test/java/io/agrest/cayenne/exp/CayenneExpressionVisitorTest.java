package io.agrest.cayenne.exp;

import io.agrest.exp.parser.AgExpressionParser;
import io.agrest.protocol.Exp;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CayenneExpressionVisitorTest {

    static CayenneExpressionVisitor visitor;

    @BeforeAll
    static void init() {
        visitor = new CayenneExpressionVisitor();
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource
    void visit_checkReturnedType(Exp agrestExp, Class<? extends Expression> cayenneExpExpectedType) {
        Expression cayenneExp = agrestExp.accept(visitor, null);
        assertEquals(cayenneExpExpectedType, cayenneExp.getClass());
    }

    static Iterable<Arguments> visit_checkReturnedType() {
        List<Arguments> argsList = List.of(
                Arguments.of("abs(1)", ASTAbs.class),
                Arguments.of("1 + 2", ASTAdd.class),
                Arguments.of("t.isA = true and t.isB = true", ASTAnd.class),
                Arguments.of("t.value between 10 and 20", ASTBetween.class),
                Arguments.of("0xFF & 0x01", ASTBitwiseAnd.class),
                Arguments.of("0xFF << 2", ASTBitwiseLeftShift.class),
                Arguments.of("~0xA7", ASTBitwiseNot.class),
                Arguments.of("0xFF | 0x01", ASTBitwiseOr.class),
                Arguments.of("0xFF >> 2", ASTBitwiseRightShift.class),
                Arguments.of("0xFF ^ 0x01", ASTBitwiseXor.class),
                Arguments.of("concat(t.v, '10')", ASTConcat.class),
                Arguments.of("currentDate()", ASTCurrentDate.class),
                Arguments.of("currentTime()", ASTCurrentTime.class),
                Arguments.of("currentTimestamp()", ASTCurrentTimestamp.class),
                Arguments.of("t.value / 2", ASTDivide.class),
                Arguments.of("t.v1 = t.v2", ASTEqual.class),
                Arguments.of("day(t.dateTime)", ASTExtract.class),
                Arguments.of("false", ASTFalse.class),
                Arguments.of("t.v > 0", ASTGreater.class),
                Arguments.of("t.v >= 0", ASTGreaterOrEqual.class),
                Arguments.of("t.v in (0, 5)", ASTIn.class),
                Arguments.of("length(a.v)", ASTLength.class),
                Arguments.of("t.v < 0", ASTLess.class),
                Arguments.of("t.v <= 0", ASTLessOrEqual.class),
                Arguments.of("t.name like '%s'", ASTLike.class),
                Arguments.of("t.name likeIgnoreCase '%s'", ASTLikeIgnoreCase.class),
                Arguments.of("locate(t.v, 'id')", ASTLocate.class),
                Arguments.of("lower(t.v)", ASTLower.class),
                Arguments.of("mod(t.v, 10)", ASTMod.class),
                Arguments.of("1 * 4", ASTMultiply.class),
                Arguments.of("$a", ASTNamedParameter.class),
                Arguments.of("-a.v", ASTNegate.class),
                Arguments.of("!(t.a = 1 and t.b = 3)", ASTNot.class),
                Arguments.of("t.value !between 10 and 20", ASTNotBetween.class),
                Arguments.of("t.v1 != t.v2", ASTNotEqual.class),
                Arguments.of("t.v !in (0, 5)", ASTNotIn.class),
                Arguments.of("t.name !like '%s'", ASTNotLike.class),
                Arguments.of("t.name !likeIgnoreCase '%s'", ASTNotLikeIgnoreCase.class),
                Arguments.of("a.v", ASTObjPath.class),
                Arguments.of("t.isA = true or t.isB = true", ASTOr.class),
                Arguments.of("1.2", ASTScalar.class),
                Arguments.of("1", ASTScalar.class),
                Arguments.of("null", ASTScalar.class),
                Arguments.of("'value'", ASTScalar.class),
                Arguments.of("sqrt(2)", ASTSqrt.class),
                Arguments.of("substring(a.v, 3)", ASTSubstring.class),
                Arguments.of("3 - 1", ASTSubtract.class),
                Arguments.of("trim(a.v)", ASTTrim.class),
                Arguments.of("true", ASTTrue.class),
                Arguments.of("upper(t.v)", ASTUpper.class)
        );
        for (Arguments args : argsList) {
            try {
                args.get()[0] = AgExpressionParser.parse((String) args.get()[0]);
            } catch (Exception e) {
                System.err.println("Expression string: " + args.get()[0]);
                throw e;
            }
        }
        return argsList;
    }
}