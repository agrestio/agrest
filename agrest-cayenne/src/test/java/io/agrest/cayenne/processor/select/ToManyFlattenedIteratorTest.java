package io.agrest.cayenne.processor.select;

import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

public class ToManyFlattenedIteratorTest {

    private ObjectContext context;

    @BeforeEach
    public void before() {
        context = mock(ObjectContext.class);
    }

    @Test
    public void testIterator_Empty() {

        List<E2> e2s = Collections.emptyList();

        ToManyFlattenedIterator<DataObject> it = new ToManyFlattenedIterator<>(e2s.iterator(), E2.E3S.getName());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIterator() {

        List<E2> e2s = asList(newE2("a", 1), newE2("b", 0), newE2("c", 2));

        ToManyFlattenedIterator<DataObject> it = new ToManyFlattenedIterator<>(e2s.iterator(), E2.E3S.getName());

        List<E3> e3s = new ArrayList<>();
        while (it.hasNext()) {
            e3s.add((E3) it.next());
        }

        assertEquals(3, e3s.size());
        assertEquals("a_e3_0", e3s.get(0).getName());
        assertEquals("c_e3_0", e3s.get(1).getName());
        assertEquals("c_e3_1", e3s.get(2).getName());
    }

    private E2 newE2(String label, int e3Count) {

        E2 e2 = new E2();
        e2.setName(label);
        e2.setObjectContext(context);

        List<E3> relationship = new ArrayList<>(e3Count);
        for (int i = 0; i < e3Count; i++) {
            relationship.add(newE3(label + "_e3_" + i));
        }

        e2.writePropertyDirectly(E2.E3S.getName(), relationship);

        return e2;
    }

    private E3 newE3(String label) {
        E3 e3 = new E3();
        e3.setName(label);
        e3.setObjectContext(context);
        return e3;
    }
}
