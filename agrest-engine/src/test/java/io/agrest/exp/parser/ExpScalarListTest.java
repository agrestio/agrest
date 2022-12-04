package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

@Disabled("To be discussed")
class ExpScalarListTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpScalarList.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                // TODO: Why fail?
                "1,2",
                "1, 2",
                "1, 2, 3"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of(",", AgException.class),
                Arguments.of("1, ", AgException.class),
                Arguments.of(", 2", AgException.class),
                Arguments.of("1 2", AgException.class),
                Arguments.of("1 ,, 1", AgException.class)
        );
    }
}