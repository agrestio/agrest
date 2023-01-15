package io.agrest.runtime;

import io.agrest.access.PathChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AgRuntimeBuilder_MaxPathDepthTest {

    @Test
    public void testDefault() {
        AgRuntime runtime = AgRuntime
                .builder()
                .build();

        assertEquals(100, runtime.service(PathChecker.class).getDepth());
    }

    @Test
    public void testOverride() {
        AgRuntime runtime = AgRuntime
                .builder()
                .maxPathDepth(3)
                .build();

        assertEquals(3, runtime.service(PathChecker.class).getDepth());
    }

    @Test
    public void testOverrideZero() {
        AgRuntime runtime = AgRuntime
                .builder()
                .maxPathDepth(0)
                .build();

        assertEquals(0, runtime.service(PathChecker.class).getDepth());
    }

    @Test
    public void testNegative() {
        assertThrows(IllegalArgumentException.class, () -> AgRuntime
                .builder()
                .maxPathDepth(-1));
    }
}
