package io.agrest.exp.parser;

import io.agrest.protocol.Exp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractExpTest {

    protected ExpTestVisitor visitor;

    @BeforeAll
    void init() {
        visitor = provideVisitor();
    }

    abstract ExpTestVisitor provideVisitor();

    abstract Stream<String> parseExp();

    abstract Stream<Arguments> parseExpThrows();

    abstract Stream<Arguments> stringifyRaw();

    protected Stream<Arguments> stringify() {
        return stringifyRaw().peek(args -> args.get()[0] = Exp.from((String) args.get()[0]));
    }

    @ParameterizedTest
    @MethodSource
    void parseExp(String expString) {
        parseExpString(expString);
    }

    @ParameterizedTest
    @MethodSource
    void stringify(Exp exp, String expected) {
        assertEquals(expected, exp.toString());
    }

    @ParameterizedTest
    @MethodSource
    void parseExpThrows(String expString, Class<? extends Throwable> throwableType) {
        assertThrows(throwableType, () -> parseExpString(expString));
    }

    protected void parseExpString(String expString) {
        Exp expression = AgExpressionParser.parse(expString);
        assertNotNull(expression);
        assertEquals(visitor.getNodeType(), expression.getClass());
    }
}
