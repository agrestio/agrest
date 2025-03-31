package io.agrest.cayenne.compiler;

import io.agrest.cayenne.cayenne.inheritance.Aie1Sub1;
import io.agrest.cayenne.cayenne.inheritance.Aie1Sub1Sub1;
import io.agrest.cayenne.cayenne.inheritance.Aie1Sub2;
import io.agrest.cayenne.cayenne.inheritance.Aie1Super;
import io.agrest.cayenne.unit.inheritance.InheritanceNoDbTest;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CayenneCompiler_Inheritance_AnnotatedIT extends InheritanceNoDbTest {

    @Test
    public void aie1Super() {
        AgEntity<Aie1Super> e = getAgEntity(Aie1Super.class);
        String attributes = e.getAttributes().stream()
                .sorted(Comparator.comparing(AgAttribute::getName))
                .map(AgAttribute::getName)
                .collect(Collectors.joining(","));

        String readables = e.getAttributes().stream()
                .sorted(Comparator.comparing(AgAttribute::getName))
                .map(AgAttribute::isReadable)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        assertEquals("a0,type", attributes);
        assertEquals("false,true", readables);
    }

    @Test
    public void aie1Sub2() {
        AgEntity<Aie1Sub2> sub2 = getAgEntity(Aie1Sub2.class);
        String attributes = sub2.getAttributes().stream()
                .sorted(Comparator.comparing(AgAttribute::getName))
                .map(AgAttribute::getName)
                .collect(Collectors.joining(","));

        String readables = sub2.getAttributes().stream()
                .sorted(Comparator.comparing(AgAttribute::getName))
                .map(AgAttribute::isReadable)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        assertEquals("a0,a2,type", attributes, "Must include own and inherited attributes");
        assertEquals("false,true,true", readables);
    }

    @Test
    public void aie1Sub1() {
        AgEntity<Aie1Sub1> sub1 = getAgEntity(Aie1Sub1.class);

        String attributes = sub1.getAttributes().stream()
                .sorted(Comparator.comparing(AgAttribute::getName))
                .map(AgAttribute::getName)
                .collect(Collectors.joining(","));

        String relationships = sub1.getRelationships().stream()
                .sorted(Comparator.comparing(AgRelationship::getName))
                .map(AgRelationship::getName)
                .sorted()
                .collect(Collectors.joining(","));

        String attributesReadable = sub1.getAttributes().stream()
                .sorted(Comparator.comparing(AgAttribute::getName))
                .map(AgAttribute::isReadable)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        assertEquals("a0,a1,type", attributes, "Must include own and inherited attributes");
        assertEquals("true,true,true", attributesReadable);
        assertEquals("ie2,ie3s", relationships, "Must include own and inherited relationships");
    }

    @Test
    public void aie1Sub1Sub1() {
        AgEntity<Aie1Sub1Sub1> sub1Sub1 = getAgEntity(Aie1Sub1Sub1.class);

        String attributes = sub1Sub1.getAttributes().stream()
                .sorted(Comparator.comparing(AgAttribute::getName))
                .map(AgAttribute::getName)
                .collect(Collectors.joining(","));

        String readables = sub1Sub1.getAttributes().stream()
                .sorted(Comparator.comparing(AgAttribute::getName))
                .map(AgAttribute::isReadable)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        assertEquals("a0,a1,a3,type", attributes, "Must include own and inherited attributes");
        assertEquals("true,true,true,true", readables);
    }
}
