package com.nhl.link.rest.constraints;

import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConstraintsBuilderTest extends TestWithCayenneMapping {

    @Test
    public void testExcludeAll() {
        ConstraintsBuilder<E4> tc = Constraint.excludeAll(E4.class);
        ConstrainedLrEntity<E4> result = tc.apply(getLrEntity(E4.class));

        assertNotNull(result);
        assertTrue(result.getAttributes().isEmpty());
        assertTrue(result.getQueryParams().isEmpty());
        assertTrue(result.getChildren().isEmpty());
        assertFalse(result.isIdIncluded());
    }

    @Test
    public void testIdOnly() {
        ConstraintsBuilder<E4> tc = Constraint.idOnly(E4.class);
        ConstrainedLrEntity<E4> result = tc.apply(getLrEntity(E4.class));

        assertNotNull(result);
        assertTrue(result.getAttributes().isEmpty());
        assertTrue(result.getQueryParams().isEmpty());
        assertTrue(result.getChildren().isEmpty());
        assertTrue(result.isIdIncluded());
    }

    @Test
    public void testIdAndAttributes() {

        ConstraintsBuilder<E4> tc = Constraint.idAndAttributes(E4.class);
        ConstrainedLrEntity<E4> result = tc.apply(getLrEntity(E4.class));

        assertNotNull(result);
        assertEquals(getLrEntity(E4.class).getAttributes().size(), result.getAttributes().size());
        assertFalse(result.getQueryParams().isEmpty());
        assertTrue(result.getChildren().isEmpty());
        assertTrue(result.isIdIncluded());
    }

    @Test
    public void testExcludeProperties() {

        ConstraintsBuilder<E4> tc = Constraint.idAndAttributes(E4.class).excludeProperties(E4.C_BOOLEAN, E4.C_DECIMAL);
        ConstrainedLrEntity<E4> result = tc.apply(getLrEntity(E4.class));

        assertNotNull(result);
        assertEquals(getLrEntity(E4.class).getAttributes().size() - 2, result.getAttributes().size());
        assertFalse(result.getQueryParams().isEmpty());
        assertTrue(result.getChildren().isEmpty());
        assertTrue(result.isIdIncluded());
    }
}
