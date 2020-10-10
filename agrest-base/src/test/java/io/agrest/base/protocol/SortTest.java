package io.agrest.base.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SortTest {

    @Test
    public void testEquals() {
        Sort s1 = new Sort("a", Dir.ASC);
        Sort s2 = new Sort("a", Dir.ASC);
        Sort s3 = new Sort("b", Dir.ASC);
        Sort s4 = new Sort("a", Dir.ASC_CI);

        assertEquals(s1, s1);
        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
        assertNotEquals(s1, s4);
    }
}
