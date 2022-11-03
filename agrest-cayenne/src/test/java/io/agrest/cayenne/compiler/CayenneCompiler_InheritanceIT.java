package io.agrest.cayenne.compiler;

import io.agrest.cayenne.cayenne.inheritance.Ie1Sub1;
import io.agrest.cayenne.cayenne.inheritance.Ie1Sub2;
import io.agrest.cayenne.cayenne.inheritance.Ie1Super;
import io.agrest.cayenne.cayenne.inheritance.Ie2;
import io.agrest.cayenne.unit.inheritance.InheritanceNoDbTest;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CayenneCompiler_InheritanceIT extends InheritanceNoDbTest {

    @Test
    public void testGetAttributes_Sub() {
        AgEntity<Ie1Sub2> ieSub2 = getAgEntity(Ie1Sub2.class);
        String as = ieSub2.getAttributes().stream()
                .map(AgAttribute::getName)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("a0,a2,type", as, "Must include own and inherited attributes");
    }

    @Test
    public void testGetAllAttributes_Sub() {
        AgEntity<Ie1Sub2> ieSub2 = getAgEntity(Ie1Sub2.class);
        String as = ieSub2.getAttributesInHierarchy().stream()
                .map(AgAttribute::getName)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("a0,a2,type", as, "Must include own and inherited attributes");
    }

    @Test
    public void testGetAllAttributes_Super() {
        String as = getAgEntity(Ie1Super.class).getAttributesInHierarchy().stream()
                .map(AgAttribute::getName)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("a0,a1,a2,a3,type", as, "Must include own, inherited and subclass attributes");
    }

    @Test
    public void testGetRelationships_Sub() {
        AgEntity<Ie1Sub1> ieSub1 = getAgEntity(Ie1Sub1.class);
        String rs = ieSub1.getRelationships().stream()
                .map(AgRelationship::getName)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("ie2,ie3s", rs, "Must include own and inherited relationships");
    }

    @Test
    public void testGetSubEntities_Super() {
        String es = getAgEntity(Ie1Super.class).getSubEntities().stream()
                .map(AgEntity::getName)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("Ie1Sub1,Ie1Sub1Sub1,Ie1Sub2", es);
    }

    @Test
    public void testGetSubEntities_Sub() {
        assertTrue(getAgEntity(Ie1Sub2.class).getSubEntities().isEmpty());
    }

    @Test
    public void testGetSubEntities_NoInheritance() {
        assertTrue(getAgEntity(Ie2.class).getSubEntities().isEmpty());
    }
}
