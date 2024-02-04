package io.agrest.cayenne.compiler;

import io.agrest.cayenne.cayenne.inheritance.Ie1Sub1;
import io.agrest.cayenne.cayenne.inheritance.Ie1Sub1Sub1;
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
    public void getAttributes_Sub() {
        AgEntity<Ie1Sub2> ieSub2 = getAgEntity(Ie1Sub2.class);
        String as = ieSub2.getAttributes().stream()
                .map(AgAttribute::getName)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("a0,a2,type", as, "Must include own and inherited attributes");
    }

    @Test
    public void getAttributes_SubSub() {
        AgEntity<Ie1Sub1Sub1> ieSub1Sub1 = getAgEntity(Ie1Sub1Sub1.class);
        String as = ieSub1Sub1.getAttributes().stream()
                .map(AgAttribute::getName)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("a0,a1,a3,type", as, "Must include own and inherited attributes");
    }

    @Test
    public void getAttributes_Super() {
        String as = getAgEntity(Ie1Super.class).getAttributes().stream()
                .map(AgAttribute::getName)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("a0,type", as, "Must include own, inherited and subclass attributes");
    }

    @Test
    public void getRelationships_Sub() {
        AgEntity<Ie1Sub1> ieSub1 = getAgEntity(Ie1Sub1.class);
        String rs = ieSub1.getRelationships().stream()
                .map(AgRelationship::getName)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("ie2,ie3s", rs, "Must include own and inherited relationships");
    }

    @Test
    public void getSubEntities_Super() {
        String es1 = getAgEntity(Ie1Super.class).getSubEntities().stream()
                .map(AgEntity::getName)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("Ie1Sub1,Ie1Sub2", es1, "Must include direct sub-entities");

        String es2 = getAgEntity(Ie1Sub1.class).getSubEntities().stream()
                .map(AgEntity::getName)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("Ie1Sub1Sub1", es2, "Must include direct sub-entities");
    }

    @Test
    public void getSubEntities_Sub() {
        assertTrue(getAgEntity(Ie1Sub2.class).getSubEntities().isEmpty());
    }

    @Test
    public void getSubEntities_NoInheritance() {
        assertTrue(getAgEntity(Ie2.class).getSubEntities().isEmpty());
    }
}
