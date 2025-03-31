package io.agrest.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SortTest {

    @Test
    public void equals() {
        Sort s1 = new Sort("a", Direction.asc);
        Sort s2 = new Sort("a", Direction.asc);
        Sort s3 = new Sort("b", Direction.asc);
        Sort s4 = new Sort("a", Direction.asc_ci);

        assertEquals(s1, s1);
        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
        assertNotEquals(s1, s4);
    }
}
