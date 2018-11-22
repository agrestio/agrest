package io.agrest.constraints;

import io.agrest.it.fixture.cayenne.E4;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConstraintsBuilderTest extends TestWithCayenneMapping {

    @Test
    public void testExcludeAll() {
        ConstraintsBuilder<E4> tc = Constraint.excludeAll(E4.class);
        ConstrainedAgEntity<E4> result = tc.apply(getAgEntity(E4.class));

        assertNotNull(result);
        assertTrue(result.getAttributes().isEmpty());
        assertTrue(result.getChildren().isEmpty());
        assertFalse(result.isIdIncluded());
    }

    @Test
    public void testIdOnly() {
        ConstraintsBuilder<E4> tc = Constraint.idOnly(E4.class);
        ConstrainedAgEntity<E4> result = tc.apply(getAgEntity(E4.class));

        assertNotNull(result);
        assertTrue(result.getAttributes().isEmpty());
        assertTrue(result.getChildren().isEmpty());
        assertTrue(result.isIdIncluded());
    }

    @Test
    public void testIdAndAttributes() {

        ConstraintsBuilder<E4> tc = Constraint.idAndAttributes(E4.class);
        ConstrainedAgEntity<E4> result = tc.apply(getAgEntity(E4.class));

        assertNotNull(result);
        Assert.assertEquals(getAgEntity(E4.class).getAttributes().size(), result.getAttributes().size());
        assertTrue(result.getChildren().isEmpty());
        assertTrue(result.isIdIncluded());
    }

    @Test
    public void testExcludeProperties() {

        ConstraintsBuilder<E4> tc = Constraint.idAndAttributes(E4.class).excludeProperties(E4.C_BOOLEAN.getName(), E4.C_DECIMAL.getName());
        ConstrainedAgEntity<E4> result = tc.apply(getAgEntity(E4.class));

        assertNotNull(result);
        Assert.assertEquals(getAgEntity(E4.class).getAttributes().size() - 2, result.getAttributes().size());
        assertTrue(result.getChildren().isEmpty());
        assertTrue(result.isIdIncluded());
    }
}
