package com.nhl.link.rest.runtime.parser.pointer;

import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E5;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PointerTest extends TestWithCayenneMapping {

    private LrPointerService pointerService;

    @Before
    public void setUp() {
        pointerService = new LrPointerService(metadataService);
    }

    @Test
    public void testDecoding_InstancePointer() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e1, "1");
        assertEquals(PointerType.INSTANCE, pointer.getType());
        assertEquals(E1.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_AttributePointer() throws Exception {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e1, E1.DESCRIPTION.getName());
        assertEquals(PointerType.ATTRIBUTE, pointer.getType());

        Class<?> targetType = Class.forName(e1.getAttribute(E1.DESCRIPTION.getName()).getJavaType());
        assertEquals(targetType, pointer.getTargetType());
    }

    @Test
    public void testDecoding_ToOneRelationshipPointer_Implicit() {

        LrEntity<E3> e3 = getLrEntity(E3.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e3, E3.E2.getName());
        assertEquals(PointerType.IMPLICIT_TO_ONE_RELATIONSHIP, pointer.getType());
        assertEquals(E2.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_ToOneRelationshipPointer_Explicit() {

        LrEntity<E3> e3 = getLrEntity(E3.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e3,
                Pointers.buildRelationship(E3.E2.getName(), "2"));
        assertEquals(PointerType.EXPLICIT_TO_ONE_RELATIONSHIP, pointer.getType());
        assertEquals(E2.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_ToManyRelationshipPointer() {

        LrEntity<E2> e2 = getLrEntity(E2.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2,
                Pointers.buildRelationship(E2.E3S.getName(), "3"));
        assertEquals(PointerType.TO_MANY_RELATIONSHIP, pointer.getType());
        assertEquals(E3.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_CompoundInstancePointer() {

        LrEntity<E2> e2 = getLrEntity(E2.class);
        String path = Pointers.buildPath(Pointers.buildRelationship(E2.E3S.getName(),"3"),"33");
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, path);
        assertEquals(PointerType.INSTANCE, pointer.getType());
        assertEquals(E3.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_CompoundAttributePointer() throws Exception {

        LrEntity<E2> e2 = getLrEntity(E2.class);
        LrEntity<E3> e3 = getLrEntity(E3.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e3,
                Pointers.buildPath(E3.E2.getName(),E2.ADDRESS.getName()));
        assertEquals(PointerType.ATTRIBUTE, pointer.getType());

        Class<?> targetType = Class.forName(e2.getAttribute("address").getJavaType());
        assertEquals(targetType, pointer.getTargetType());
    }

    @Test
    public void testDecoding_CompoundToOneRelationshipPointer_Implicit() {

        LrEntity<E2> e2 = getLrEntity(E2.class);
        String path = Pointers.buildPath(Pointers.buildRelationship(E2.E3S.getName(),"3"),E3.E5.getName());
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, path);
        assertEquals(PointerType.IMPLICIT_TO_ONE_RELATIONSHIP, pointer.getType());
        assertEquals(E5.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_CompoundToOneRelationshipPointer_Explicit() {

        LrEntity<E2> e2 = getLrEntity(E2.class);
        String path = Pointers.buildPath(
                Pointers.buildRelationship(E2.E3S.getName(),"3"), Pointers.buildRelationship(E3.E5.getName(),"5"));
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, path);
        assertEquals(PointerType.EXPLICIT_TO_ONE_RELATIONSHIP, pointer.getType());
        assertEquals(E5.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_CompoundToManyRelationshipPointer() {

        LrEntity<E3> e3 = getLrEntity(E3.class);
        String path = Pointers.buildPath(E3.E5.getName(), Pointers.buildRelationship(E5.E2S.getName(),"2"));
        LrPointer pointer = new PointerParser(pointerService).getPointer(e3, path);
        assertEquals(PointerType.TO_MANY_RELATIONSHIP, pointer.getType());
        // TODO: Fix error in Cayenne mapping (obviously e2s should be of E2 type)
        assertEquals(E3.class, pointer.getTargetType());
    }

    @Test(expected = Exception.class)
    public void testDecoding_CompoundToManyRelationshipPointer_WithoutId() {

        LrEntity<E3> e3 = getLrEntity(E3.class);
        new PointerParser(pointerService).getPointer(e3, Pointers.buildPath(E3.E5.getName(),E5.E2S.getName()));
    }

    @Test(expected = Exception.class)
    public void testDecoding_ToManyRelationshipPointer_WithoutId() {

        LrEntity<E2> e2 = getLrEntity(E2.class);
        new PointerParser(pointerService).getPointer(e2, E2.E3S.getName());
    }

    @Test(expected = Exception.class)
    public void testDecoding_RelationshipPointer_AfterAttribute() {

        LrEntity<E3> e3 = getLrEntity(E3.class);
        new PointerParser(pointerService).getPointer(e3, Pointers.buildPath(E3.NAME.getName(),E3.E5.getName()));
    }

    @Test(expected = Exception.class)
    public void testDecoding_InvalidPath1() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        new PointerParser(pointerService).getPointer(e1, "");
    }

    @Test(expected = Exception.class)
    public void testDecoding_InvalidPath2() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        new PointerParser(pointerService).getPointer(e1, Pointers.PATH_SEPARATOR);
    }

    @Test(expected = Exception.class)
    public void testDecoding_InvalidPath3() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        new PointerParser(pointerService).getPointer(e1, Pointers.RELATIONSHIP_SEPARATOR);
    }

    @Test(expected = Exception.class)
    public void testDecoding_InvalidPath4() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        new PointerParser(pointerService).getPointer(e1, Pointers.PATH_SEPARATOR + "3");
    }

    @Test(expected = Exception.class)
    public void testDecoding_InvalidPath5() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        new PointerParser(pointerService).getPointer(e1, "3" + Pointers.PATH_SEPARATOR);
    }

    @Test(expected = Exception.class)
    public void testDecoding_InvalidPath6() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        new PointerParser(pointerService).getPointer(e1, Pointers.PATH_SEPARATOR + Pointers.PATH_SEPARATOR);
    }
}
