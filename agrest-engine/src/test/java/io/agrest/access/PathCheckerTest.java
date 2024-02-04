package io.agrest.access;

import io.agrest.AgException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PathCheckerTest {

    @Test
    public void checkExceedsDepth_Zero() {

        PathChecker d = PathChecker.of(0);
        d.exceedsDepth(null);
        d.exceedsDepth("");
        d.exceedsDepth("a");

        assertThrows(AgException.class, () -> d.exceedsDepth("a.b"));
    }

    @Test
    public void checkExceedsDepth() {

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
