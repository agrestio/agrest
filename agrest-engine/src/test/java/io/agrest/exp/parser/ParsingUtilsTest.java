package io.agrest.exp.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ParsingUtilsTest {

    @MethodSource
    @ParameterizedTest
    void parsePath(ExpPath path, String processedPath, Map<String, String> aliases) {
        ParsingUtils.processPathAliases(path);
        assertEquals(processedPath, path.getPath());
        assertEquals(aliases, path.getPathAliases());
    }

    public static Stream<Arguments> parsePath() {
        return Stream.of(
                Arguments.of(new ExpPath("a"), "a", Map.of()),
                Arguments.of(new ExpPath("a.b"), "a.b", Map.of()),
                Arguments.of(new ExpPath("a.b.c"), "a.b.c", Map.of()),

                Arguments.of(new ExpPath("a#x1"), "x1", Map.of("x1", "a")),
                Arguments.of(new ExpPath("a#x1.b"), "x1.b", Map.of("x1", "a")),
                Arguments.of(new ExpPath("a.b#x2"), "a.x2", Map.of("x2", "b")),
                Arguments.of(new ExpPath("a.b#x2.c"), "a.x2.c", Map.of("x2", "b")),
                Arguments.of(new ExpPath("a#x1.b#x2.c"), "x1.x2.c", Map.of("x1", "a", "x2", "b")),
                Arguments.of(new ExpPath("a#x1.b#x2.c#x3"), "x1.x2.x3", Map.of("x1", "a", "x2", "b", "x3", "c")),

                Arguments.of(new ExpPath("db:a"), "db:a", Map.of()),
                Arguments.of(new ExpPath("db:a.b"), "db:a.b", Map.of()),
                Arguments.of(new ExpPath("db:a.b.c"), "db:a.b.c", Map.of()),

                Arguments.of(new ExpPath("db:a#x1"), "db:x1", Map.of("x1", "a")),
                Arguments.of(new ExpPath("db:a#x1.b"), "db:x1.b", Map.of("x1", "a")),
                Arguments.of(new ExpPath("db:a.b#x2"), "db:a.x2", Map.of("x2", "b")),
                Arguments.of(new ExpPath("db:a.b#x2.c"), "db:a.x2.c", Map.of("x2", "b")),
                Arguments.of(new ExpPath("db:a#x1.b#x2.c"), "db:x1.x2.c", Map.of("x1", "a", "x2", "b")),
                Arguments.of(new ExpPath("db:a#x1.b#x2.c#x3"), "db:x1.x2.x3", Map.of("x1", "a", "x2", "b", "x3", "c"))
        );
    }

}