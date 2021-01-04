package io.agrest;

import io.agrest.base.protocol.Exp;
import io.agrest.meta.AgEntity;
import io.agrest.pojo.model.P1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ResourceEntityTest {

    @Test
    public void testQualifier() {
        @SuppressWarnings("unchecked")
        ResourceEntity<P1> e = new RootResourceEntity<>(mock(AgEntity.class), null);
        assertNull(e.getQualifier());

        e.andQualifier(Exp.simple("a = 1"));
        assertEquals(Exp.simple("a = 1"), e.getQualifier());

        e.andQualifier(Exp.simple("b = 2"));
        assertEquals(Exp.simple("a = 1").and(Exp.simple("b = 2")), e.getQualifier());
    }
}
