package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.provider.Arguments;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

class ExpScalarIntTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpScalar.class);
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
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("-3", AssertionFailedError.class),
                Arguments.of("0x", AgException.class),
                Arguments.of("08", AgException.class),
                Arguments.of("0xG", AgException.class),
                Arguments.of("2147483648", AgException.class),          // Integer.MAX_VALUE + 1
                Arguments.of("9223372036854775808l", AgException.class) // Long.MAX_VALUE + 1
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("1", "1"),
                Arguments.of(" 1  ", "1"),

                // Integer.MAX_VALUE
                Arguments.of("2147483647", "2147483647"),

                // Integer.MAX_VALUE + 1
                Arguments.of("2147483648L", "2147483648"),

                // Long.MAX_VALUE + 1
                Arguments.of("9223372036854775808H", "9223372036854775808"),

                Arguments.of("01234567", "342391"),
                Arguments.of("0x12345678", "305419896"),
                Arguments.of("0x09abcdef", "162254319")
        );
    }
}
