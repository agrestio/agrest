package io.agrest.access;

import io.agrest.AgException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PathCheckerTest {

    @Test
    public void testCheckExceedsDepth() {

        PathChecker d = PathChecker.of(3);
        d.exceedsDepth(null);
        d.exceedsDepth("");
        d.exceedsDepth("a");
        d.exceedsDepth("a.b");
        d.exceedsDepth("a.b.c");
        d.exceedsDepth("a.b.c.d");

        assertThrows(AgException.class, () -> d.exceedsDepth("a.b.c.d.e"));
    }
}
