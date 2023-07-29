package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExpScalarListTest extends AbstractExpTest {

    @Override
    protected ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpScalarList.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "$a, $b",
                "$a,  $b",
                "$a",
                "$a, $b, $c",
                "1, 2",
                "1, 2.2",
                "1, TRUE",
                "'1', '2'"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("$a,", AgException.class),
                Arguments.of("$a, $b,", AgException.class),
                Arguments.of(",", AgException.class),
                Arguments.of(",$b", AgException.class),
                Arguments.of("null, $b", AgException.class),
                Arguments.of("$a, currentDate()", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("$a, $b", "$a, $b"),
                Arguments.of("$a,  $b", "$a, $b"),
                Arguments.of("$a, $b, $c", "$a, $b, $c")
        );
    }

    @Override
    protected Stream<Arguments> stringify() {
        // Wrap scalar list in 'In' expression to meet grammar requirements.
        return stringifyRaw().peek(args -> args.get()[0] = Exp.parse("a in (" + args.get()[0] + ")"));
    }

    Stream<Arguments> countChildren() {
        return Stream.of(
                Arguments.of("$a", 1),
                Arguments.of("$a, $b", 2),
                Arguments.of("$a, $b, $c", 3)
        ).peek(
                // Wrap scalar list in 'In' expression to meet grammar requirements.
                args -> args.get()[0] = Exp.parse("a in (" + args.get()[0] + ")")
        );
    }

    @Override
    protected void parseExpString(String expString) {
        // Wrap scalar list in 'In' expression to meet grammar requirements.
        expString = "a in (" + expString +  ")";

        // Unwrap scalar list and make generic test.
        Exp expression = AgExpressionParser.parse(expString);
        expression = ((SimpleNode) expression).children[1];
        assertNotNull(expression);
        assertEquals(visitor.getNodeType(), expression.getClass());
    }

    @Override
    @ParameterizedTest
    @MethodSource("stringify")
    void stringify(Exp exp, String expected) {
        // Unwrap scalar list and make generic test.
        exp = ((SimpleNode) exp).children[1];
        assertEquals(expected, exp.toString());
    }

    @ParameterizedTest
    @MethodSource
    void countChildren(Exp exp, int expected) {
        // Unwrap scalar list.
        exp = ((SimpleNode) exp).children[1];
        assertEquals(((SimpleNode) exp).children.length, expected);
    }
}
