package io.agrest.access;

import io.agrest.AgException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MaxPathDepthTest {

    @Test
    public void testCheckExceedsDepth() {

        MaxPathDepth d = MaxPathDepth.of(3);
        d.checkExceedsDepth(null);
        d.checkExceedsDepth("");
        d.checkExceedsDepth("a");
        d.checkExceedsDepth("a.b");
        d.checkExceedsDepth("a.b.c");
        d.checkExceedsDepth("a.b.c.d");

        assertThrows(AgException.class, () -> d.checkExceedsDepth("a.b.c.d.e"));
    }
}
