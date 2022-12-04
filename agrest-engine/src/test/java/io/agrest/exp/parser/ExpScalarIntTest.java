package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

class ExpScalarIntTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpScalarInt.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "0",
                "1",

                // Integer.MAX_VALUE
                "2147483647",

                // Integer.MAX_VALUE + 1
                "2147483648L",
                "2147483648l",

                // Long.MAX_VALUE + 1
                "9223372036854775808H",
                "9223372036854775808h",
                "01234567",
                "0x12345678",
                "0x09abcdef",
                "0x09ABCDEF",
                "0X1",
                "+1"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("-3", AssertionFailedError.class),

                Arguments.of("0x", AgException.class),
                Arguments.of("08", AgException.class),
                Arguments.of("0xG", AgException.class),

                // Integer.MAX_VALUE + 1
                Arguments.of("2147483648", NumberFormatException.class),

                // Long.MAX_VALUE + 1
                Arguments.of("9223372036854775808l", NumberFormatException.class)
        );
    }
}