package io.agrest.resolver;

import io.agrest.pojo.model.P3;
import io.agrest.pojo.model.P4;
import io.agrest.pojo.model.P5;
import org.apache.cayenne.ObjectContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
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
        List<P5> parents = Collections.emptyList();
        ToManyFlattenedIterator<P4> children = new ToManyFlattenedIterator<>(parents.iterator(), p -> ((P5) p).getP4s());
        assertFalse(children.hasNext());
    }

    @Test
    public void testIterator() {

        List<P5> parents = asList(newP5("a", 1), newP5("b", 0), newP5("c", 2));

        ToManyFlattenedIterator<P4> children = new ToManyFlattenedIterator<>(parents.iterator(), p -> ((P5) p).getP4s());
        List<P4> result = new ArrayList<>();
        children.forEachRemaining(result::add);

        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals("a_p3_0", result.get(0).getP3().getName());
        Assertions.assertEquals("c_p3_0", result.get(1).getP3().getName());
        Assertions.assertEquals("c_p3_1", result.get(2).getP3().getName());
    }

    private P5 newP5(String label, int e4Count) {

        P5 p5 = new P5();

        List<P4> relationship = new ArrayList<>(e4Count);
        for (int i = 0; i < e4Count; i++) {
            relationship.add(newP4(label + "_p3_" + i));
        }

        p5.setP4s(relationship);

        return p5;
    }

    private P4 newP4(String label) {
        P4 p4 = new P4();

        P3 p3 = new P3();
        p3.setName(label);

        p4.setP3(p3);
        return p4;
    }
}
