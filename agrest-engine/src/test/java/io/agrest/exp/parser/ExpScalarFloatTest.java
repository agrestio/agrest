package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

class ExpScalarFloatTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpScalar.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "1.0",
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

                // 10 * Double.MAX_VALUE
                "1.7976931348623157e+309b",
                "1.7976931348623157e+309B"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("1.7976931348623157e+308f", AgException.class),    // Double.MAX_VALUE
                Arguments.of("1.7976931348623157e+309d", AgException.class),    // 10 * Double.MAX_VALUE
                Arguments.of("e1", AssertionFailedError.class),
                Arguments.of("1+e1", AssertionFailedError.class),
                Arguments.of("1e1.1", AgException.class),
                Arguments.of(".", AgException.class),
                Arguments.of("0x1.F", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("1.0", "1.0"),
                Arguments.of("  1.0 ", "1.0"),
                Arguments.of(".1", "0.1"),
                Arguments.of("1e1", "10.0"),
                Arguments.of("1E1", "10.0"),
                Arguments.of("1.1e1", "11.0"),
                Arguments.of("3.4028235e+38f", "3.4028235E38"),
                Arguments.of("1.7976931348623157e+308", "1.7976931348623157E308"),
                Arguments.of("1.7976931348623157e+308d", "1.7976931348623157E308"),
                Arguments.of("1.7976931348623157e+309b", "1.7976931348623157E+309")
        );
    }
}
