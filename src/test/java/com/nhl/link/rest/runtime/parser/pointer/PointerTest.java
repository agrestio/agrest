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
        pointerService = new LrPointerService(metadataService, mockCayennePersister);
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
        LrPointer pointer = new PointerParser(pointerService).getPointer(e1, "description");
        assertEquals(PointerType.ATTRIBUTE, pointer.getType());

        Class<?> targetType = Class.forName(e1.getAttribute("description").getJavaType());
        assertEquals(targetType, pointer.getTargetType());
    }

    @Test
    public void testDecoding_ToOneRelationshipPointer_Implicit() {

        LrEntity<E3> e3 = getLrEntity(E3.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e3, "e2");
        assertEquals(PointerType.IMPLICIT_TO_ONE_RELATIONSHIP, pointer.getType());
        assertEquals(E2.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_ToOneRelationshipPointer_Explicit() {

        LrEntity<E3> e3 = getLrEntity(E3.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e3, "e2:2");
        assertEquals(PointerType.EXPLICIT_TO_ONE_RELATIONSHIP, pointer.getType());
        assertEquals(E2.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_ToManyRelationshipPointer() {

        LrEntity<E2> e2 = getLrEntity(E2.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, "e3s:3");
        assertEquals(PointerType.TO_MANY_RELATIONSHIP, pointer.getType());
        assertEquals(E3.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_CompoundInstancePointer() {

        LrEntity<E2> e2 = getLrEntity(E2.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, "e3s:3.33");
        assertEquals(PointerType.INSTANCE, pointer.getType());
        assertEquals(E3.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_CompoundAttributePointer() throws Exception{

        LrEntity<E2> e2 = getLrEntity(E2.class);
        LrEntity<E3> e3 = getLrEntity(E3.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e3, "e2.address");
        assertEquals(PointerType.ATTRIBUTE, pointer.getType());

        Class<?> targetType = Class.forName(e2.getAttribute("address").getJavaType());
        assertEquals(targetType, pointer.getTargetType());
    }

    @Test
    public void testDecoding_CompoundToOneRelationshipPointer_Implicit() {

        LrEntity<E2> e2 = getLrEntity(E2.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, "e3s:3.e5");
        assertEquals(PointerType.IMPLICIT_TO_ONE_RELATIONSHIP, pointer.getType());
        assertEquals(E5.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_CompoundToOneRelationshipPointer_Explicit() {

        LrEntity<E2> e2 = getLrEntity(E2.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e2, "e3s:3.e5:5");
        assertEquals(PointerType.EXPLICIT_TO_ONE_RELATIONSHIP, pointer.getType());
        assertEquals(E5.class, pointer.getTargetType());
    }

    @Test
    public void testDecoding_CompoundToManyRelationshipPointer() {

        LrEntity<E3> e3 = getLrEntity(E3.class);
        LrPointer pointer = new PointerParser(pointerService).getPointer(e3, "e5.e2s:2");
        assertEquals(PointerType.TO_MANY_RELATIONSHIP, pointer.getType());
        // TODO: Fix error in Cayenne mapping (obviously e2s should be of E2 type)
        assertEquals(E3.class, pointer.getTargetType());
    }

    @Test(expected = Exception.class)
    public void testDecoding_CompoundToManyRelationshipPointer_WithoutId() {

        LrEntity<E3> e3 = getLrEntity(E3.class);
        new PointerParser(pointerService).getPointer(e3, "e5.e2s");
    }

    @Test(expected = Exception.class)
    public void testDecoding_ToManyRelationshipPointer_WithoutId() {

        LrEntity<E2> e2 = getLrEntity(E2.class);
        new PointerParser(pointerService).getPointer(e2, "e3s");
    }

    @Test(expected = Exception.class)
    public void testDecoding_InvalidPath1() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        new PointerParser(pointerService).getPointer(e1, "");
    }

    @Test(expected = Exception.class)
    public void testDecoding_InvalidPath2() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        new PointerParser(pointerService).getPointer(e1, ".");
    }

    @Test(expected = Exception.class)
    public void testDecoding_InvalidPath3() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        new PointerParser(pointerService).getPointer(e1, ":");
    }

    @Test(expected = Exception.class)
    public void testDecoding_InvalidPath4() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        new PointerParser(pointerService).getPointer(e1, ".3");
    }

    @Test(expected = Exception.class)
    public void testDecoding_InvalidPath5() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        new PointerParser(pointerService).getPointer(e1, "3.");
    }

    @Test(expected = Exception.class)
    public void testDecoding_InvalidPath6() {

        LrEntity<E1> e1 = getLrEntity(E1.class);
        new PointerParser(pointerService).getPointer(e1, "..");
    }
}
