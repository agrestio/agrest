package io.agrest.access;

import io.agrest.junit.AgPojoTester;
import io.agrest.meta.AgEntity;
import io.agrest.pojo.model.P11;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class PropertyFilteringRulesBuilderTest {

    @BQTestTool
    static final AgPojoTester tester = AgPojoTester.builder().build();

    @Test
    public void testDefault_AllAvailable() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder();
        assertInaccessible(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)));
    }

    @Test
    public void testDefault_AllAvailable_WithOverlay() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder();
        assertInaccessible(pab.resolveInaccessible(
                tester.entity(P11.class),
                AgEntity.overlay(P11.class).redefineAttribute("x", String.class, p11 -> "a")));
    }

    @Test
    public void testEmpty() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder().empty();
        assertInaccessible(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)),
                "id", "intProp", "p6");
    }

    @Test
    public void testIdOnly() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder().idOnly();
        assertInaccessible(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)), "intProp", "p6");
    }

    @Test
    public void testId() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder().empty().id(true);
        assertInaccessible(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)), "intProp", "p6");
    }

    @Test
    public void testAllButId() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder().empty().attributes(true).relationships(true);
        assertInaccessible(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)), "id");
    }

    @Test
    public void testExcludeByName() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder().property("intProp", false);
        assertInaccessible(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)), "intProp");
    }

    @Test
    public void testExcludeByName_InOverlay() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder().property("x", false);

        assertInaccessible(pab.resolveInaccessible(
                tester.entity(P11.class),
                AgEntity.overlay(P11.class).redefineAttribute("x", String.class, p11 -> "a")), "x");
    }

    private void assertInaccessible(Set<String> actual, String... expected) {
        Set<String> expectedSet = new HashSet<>(asList(expected));
        assertEquals(expectedSet, actual);
    }
}
