package io.agrest.access;

import io.agrest.junit.AgPojoTester;
import io.agrest.meta.AgEntity;
import io.agrest.junit.pojo.P11;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class PropertyFilteringRulesBuilderTest {

    @BQTestTool
    static final AgPojoTester tester = AgPojoTester.builder().build();

    @Test
    public void testDefault_AllAvailable() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder();
        assertEquals(
                Map.of(),
                pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)));
    }

    @Test
    public void testDefault_AllAvailable_WithOverlay() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder();
        assertEquals(
                Map.of(),
                pab.resolveInaccessible(
                        tester.entity(P11.class),
                        AgEntity.overlay(P11.class).attribute("x", String.class, p11 -> "a")));
    }

    @Test
    public void testEmpty() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder().empty();
        assertEquals(
                Map.of("id", false, "intProp", false, "p6", false),
                pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)));
    }

    @Test
    public void testIdOnly() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder().idOnly();
        assertEquals(pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)),
                Map.of("intProp", false, "p6", false, "id", true));
    }

    @Test
    public void testId() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder().empty().id(true);
        assertEquals(
                Map.of("intProp", false, "p6", false, "id", true),
                pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)));
    }

    @Test
    public void testAllButId() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder().empty().attributes(true).relationships(true);
        assertEquals(
                Map.of("id", false, "p6", true, "intProp", true),
                pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)));
    }

    @Test
    public void testExcludeByName() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder().property("intProp", false);
        assertEquals(
                Map.of("intProp", false),
                pab.resolveInaccessible(tester.entity(P11.class), AgEntity.overlay(P11.class)));
    }

    @Test
    public void testExcludeByName_InOverlay() {
        PropertyFilteringRulesBuilder pab = new PropertyFilteringRulesBuilder().property("x", false);

        assertEquals(
                Map.of("x", false),
                pab.resolveInaccessible(
                        tester.entity(P11.class),
                        AgEntity.overlay(P11.class).attribute("x", String.class, p11 -> "a")));
    }
}
