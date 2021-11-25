package io.agrest.access;

import io.agrest.meta.AgEntity;
import io.agrest.pojo.model.P11;
import io.agrest.unit.AgPojoTester;
import io.agrest.unit.PojoTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyAccessBuilderTest extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = tester().build();

    @Test
    public void testDefault_AllAvailable() {
        PropertyAccessBuilder pab = new PropertyAccessBuilder();
        assertInaccessible(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)));
    }

    @Test
    public void testDefault_AllAvailable_WithOverlay() {
        PropertyAccessBuilder pab = new PropertyAccessBuilder();
        assertInaccessible(pab.resolveInaccessible(
                tester.entity(P11.class),
                AgEntity.overlay(P11.class).redefineAttribute("x", String.class, p11 -> "a")));
    }

    @Test
    public void testEmpty() {
        PropertyAccessBuilder pab = new PropertyAccessBuilder().empty();
        assertInaccessible(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)),
                "id", "intProp", "p6");
    }

    @Test
    public void testIdOnly() {
        PropertyAccessBuilder pab = new PropertyAccessBuilder().idOnly();
        assertInaccessible(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)), "intProp", "p6");
    }

    @Test
    public void testId() {
        PropertyAccessBuilder pab = new PropertyAccessBuilder().empty().id(true);
        assertInaccessible(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)), "intProp", "p6");
    }

    @Test
    public void testAllButId() {
        PropertyAccessBuilder pab = new PropertyAccessBuilder().empty().attributes(true).relationships(true);
        assertInaccessible(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)), "id");
    }

    @Test
    public void testExcludeByName() {
        PropertyAccessBuilder pab = new PropertyAccessBuilder().property("intProp", false);
        assertInaccessible(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)), "intProp");
    }

    @Test
    public void testExcludeByName_InOverlay() {
        PropertyAccessBuilder pab = new PropertyAccessBuilder().property("x", false);

        assertInaccessible(pab.resolveInaccessible(
                tester.entity(P11.class),
                AgEntity.overlay(P11.class).redefineAttribute("x", String.class, p11 -> "a")), "x");
    }

    private void assertInaccessible(Set<String> actual, String... expected) {
        Set<String> expectedSet = new HashSet<>(asList(expected));
        assertEquals(expectedSet, actual);
    }
}
