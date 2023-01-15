package io.agrest.runtime;

import io.agrest.access.MaxIncludeDepth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AgRuntimeBuilder_MaxIncludeDepthTest {

    @Test
    public void testDefault() {
        AgRuntime runtime = AgRuntime
                .builder()
                .build();

        assertEquals(100, runtime.service(MaxIncludeDepth.class).getDepth());
    }

    @Test
    public void testOverride() {
        AgRuntime runtime = AgRuntime
                .builder()
                .maxIncludeDepth(3)
                .build();

        assertEquals(3, runtime.service(MaxIncludeDepth.class).getDepth());
    }

    @Test
    public void testOverrideZero() {
        AgRuntime runtime = AgRuntime
                .builder()
                .maxIncludeDepth(0)
                .build();

        assertEquals(0, runtime.service(MaxIncludeDepth.class).getDepth());
    }

    @Test
    public void testNegative() {
        assertThrows(IllegalArgumentException.class, () -> AgRuntime
                .builder()
                .maxIncludeDepth(-1));
    }
}
