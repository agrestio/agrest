package com.nhl.link.rest.constraints;

import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConstraintsTest extends TestWithCayenneMapping {

    @Test
    public void testExcludeAll() {
        Constraints<E4> tc = Constraints.excludeAll(E4.class);
        ConstrainedLrEntity<E4> result = tc.apply(getLrEntity(E4.class));

        assertNotNull(result);
        assertTrue(result.getAttributes().isEmpty());
        assertTrue(result.getChildren().isEmpty());
        assertFalse(result.isIdIncluded());
    }

    @Test
    public void testIdOnly() {
        Constraints<E4> tc = Constraints.idOnly(E4.class);
        ConstrainedLrEntity<E4> result = tc.apply(getLrEntity(E4.class));

        assertNotNull(result);
        assertTrue(result.getAttributes().isEmpty());
        assertTrue(result.getChildren().isEmpty());
        assertTrue(result.isIdIncluded());
    }

    @Test
    public void testIdAndAttributes() {

        Constraints<E4> tc = Constraints.idAndAttributes(E4.class);
        ConstrainedLrEntity<E4> result = tc.apply(getLrEntity(E4.class));

        assertNotNull(result);
        assertEquals(getLrEntity(E4.class).getAttributes().size(), result.getAttributes().size());
        assertTrue(result.getChildren().isEmpty());
        assertTrue(result.isIdIncluded());
    }

    @Test
    public void testExcludeProperties() {

        Constraints<E4> tc = Constraints.idAndAttributes(E4.class).excludeProperties(E4.C_BOOLEAN, E4.C_DECIMAL);
        ConstrainedLrEntity<E4> result = tc.apply(getLrEntity(E4.class));

        assertNotNull(result);
        assertEquals(getLrEntity(E4.class).getAttributes().size() - 2, result.getAttributes().size());
        assertTrue(result.getChildren().isEmpty());
        assertTrue(result.isIdIncluded());
    }
}
