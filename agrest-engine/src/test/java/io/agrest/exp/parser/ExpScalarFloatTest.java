package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

class ExpScalarFloatTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpScalarFloat.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "1.0",
                ".1",
                ".1",
                "1e1",
                "1E1",
                "1.1e1",

                // Float.MAX_VALUE
                "3.4028235e+38f",
                "3.4028235e+38F",

                // Double.MAX_VALUE
                "1.7976931348623157e+308",
                "1.7976931348623157e+308d",
                "1.7976931348623157e+308D",

                // ~ 1.8 * Double.MAX_VALUE
                "1.7976931348623157e+309b",
                "1.7976931348623157e+309B",

                // Double.MAX_VALUE -> Float.POSITIVE_INFINITY
                "1.7976931348623157e+308f",

                // ~ 1.8 * Double.MAX_VALUE -> Double.POSITIVE_INFINITY
                "1.7976931348623157e+309d"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("e1", AssertionFailedError.class),
                Arguments.of("1+e1", AssertionFailedError.class),
                Arguments.of("1e1.1", AgException.class),
                Arguments.of(".", TokenMgrException.class),
                Arguments.of("0x1.F", TokenMgrException.class)

        );
    }
}