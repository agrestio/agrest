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

    private ExpTestVisitor visitor;

    @BeforeAll
    void init() {
        visitor = provideVisitor();
    }

    abstract ExpTestVisitor provideVisitor();

    abstract Stream<String> parseExp();

    abstract Stream<Arguments> parseExpThrows();

    @ParameterizedTest
    @MethodSource
    @Order(1)
    void parseExp(String expString) {
        parseExpString(expString);
    }

    private void parseExpString(String expString) {
        Exp expression = AgExpressionParser.parse(expString);
        assertNotNull(expression);
        assertEquals(visitor.getNodeType(), expression.getClass());
    }

    @ParameterizedTest
    @MethodSource
    @Order(2)
    void parseExpThrows(String expString, Class<? extends Throwable> throwableType) {
        assertThrows(throwableType, () -> parseExpString(expString));
    }
}
