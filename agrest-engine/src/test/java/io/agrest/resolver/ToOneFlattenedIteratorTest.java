package io.agrest.resolver;

import io.agrest.pojo.model.P3;
import io.agrest.pojo.model.P4;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ToOneFlattenedIteratorTest {

    @Test
    public void testIterator_Empty() {
        List<P4> parents = Collections.emptyList();
        ToOneFlattenedIterator<P4> children = new ToOneFlattenedIterator<>(parents.iterator(), p -> ((P4) p).getP3());
        assertFalse(children.hasNext());
    }

    @Test
    public void testIterator() {

        List<P4> parents = asList(
                newP4("a", true),
                newP4("b", false),
                newP4("c", true)
        );

        ToOneFlattenedIterator<P3> children = new ToOneFlattenedIterator<>(parents.iterator(), p -> ((P4) p).getP3());
        List<P3> result = new ArrayList<>();
        children.forEachRemaining(result::add);

        assertEquals(2, result.size());
        Assertions.assertEquals("a_e2", result.get(0).getName());
        Assertions.assertEquals("c_e2", result.get(1).getName());
    }

    private P4 newP4(String label, boolean e3) {

        P4 p4 = new P4();

        if (e3) {
            p4.setP3(newP3(label + "_e2"));
        }

        return p4;
    }

    private P3 newP3(String label) {
        P3 e2 = new P3();
        e2.setName(label);
        return e2;
    }
}
