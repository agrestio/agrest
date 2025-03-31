package io.agrest.runtime;

import io.agrest.access.PathChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AgRuntimeBuilder_MaxPathDepthTest {

    @Test
    public void _default() {
        AgRuntime runtime = AgRuntime
                .builder()
                .build();

        assertEquals(100, runtime.service(PathChecker.class).getDepth());
    }

    @Test
    public void override() {
        AgRuntime runtime = AgRuntime
                .builder()
                .maxPathDepth(3)
                .build();

        assertEquals(3, runtime.service(PathChecker.class).getDepth());
    }

    @Test
    public void overrideZero() {
        AgRuntime runtime = AgRuntime
                .builder()
                .maxPathDepth(0)
                .build();

        assertEquals(0, runtime.service(PathChecker.class).getDepth());
    }

    @Test
    public void negative() {
        assertThrows(IllegalArgumentException.class, () -> AgRuntime
                .builder()
                .maxPathDepth(-1));
    }
}
