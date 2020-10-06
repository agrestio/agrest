package io.agrest.cayenne.processor.select;

import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ToOneFlattenedIteratorTest {

    private ObjectContext context;

    @Before
    public void before() {
        context = mock(ObjectContext.class);
    }

    @Test
    public void testIterator_Empty() {

        List<E3> e3s = Collections.emptyList();
        ToOneFlattenedIterator<DataObject> it = new ToOneFlattenedIterator<>(e3s.iterator(), E3.E2.getName());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIterator() {

        List<E3> e3s = asList(newE3("a", true), newE3("b", false), newE3("c", true));
        ToOneFlattenedIterator<DataObject> it = new ToOneFlattenedIterator<>(e3s.iterator(), E3.E2.getName());

        List<E2> e2s = new ArrayList<>();
        while (it.hasNext()) {
            e2s.add((E2) it.next());
        }

        assertEquals(2, e2s.size());
        assertEquals("a_e2", e2s.get(0).getName());
        assertEquals("c_e2", e2s.get(1).getName());
    }

    private E3 newE3(String label, boolean e2) {

        E3 e3 = new E3();
        e3.setName(label);
        e3.setObjectContext(context);

        if(e2) {
            e3.writePropertyDirectly(E3.E2.getName(), newE2(label + "_e2"));
        }

        return e3;
    }

    private E2 newE2(String label) {
        E2 e2 = new E2();
        e2.setName(label);
        e2.setObjectContext(context);
        return e2;
    }
}
