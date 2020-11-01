package io.agrest.sencha.runtime.entity;

import io.agrest.base.protocol.Exp;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.CayenneNoDbTest;
import io.agrest.meta.AgEntity;
import io.agrest.sencha.protocol.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SenchaFilterExpressionCompilerTest extends CayenneNoDbTest {

    private AgEntity<E4> e4Entity;
    private SenchaFilterExpressionCompiler processor;

    @BeforeEach
    public void before() {
        this.processor = new SenchaFilterExpressionCompiler();
        this.e4Entity = getAgEntity(E4.class);
    }

    private void assertFromFilter(Filter filter, String expectedExp, Object... expectedParams) {
        assertEquals(new Exp(expectedExp, expectedParams), processor.fromFilter(filter));
    }

    private List<Exp> process(Filter... filters) {
        return processor.process(e4Entity, Arrays.asList(filters));
    }

    @Test
    public void testProcess_Disabled() {
        assertTrue(process(new Filter("cVarchar", "xyz", "like", true, false)).isEmpty());
    }

    @Test
    public void testProcess_MultipleFilters() {
        assertEquals(asList(
                new Exp("cVarchar likeIgnoreCase 'xyz%'"),
                new Exp("cVarchar likeIgnoreCase '123%'")
                ),
                process(
                        new Filter("cVarchar", "xyz", "like", false, false),
                        new Filter("cVarchar", "123", "like", false, false)));
    }

    @Test
    public void testProcess_MultipleFilters_Disabled() {
        assertEquals(asList(new Exp("cVarchar likeIgnoreCase '123%'")),
                process(
                        new Filter("cVarchar", "xyz", "like", true, false),
                        new Filter("cVarchar", "123", "like", false, false)));
    }

    @Test
    public void testFromFilter_ValueEscape() {
        assertFromFilter(new Filter("cVarchar", "x_%", "like", false, false), "cVarchar likeIgnoreCase 'x\\_\\%%'");
    }

    @Test
    public void testFromFilter_ValueNull() {
        assertFromFilter(new Filter("cVarchar", null, "like", false, false), "cVarchar = $a", new Object[]{null});
    }

    @Test
    public void testFromFilter_ExactMatch() {
        assertFromFilter(new Filter("cVarchar", "xyz", "like", false, true), "cVarchar = $a", "xyz");
    }

    @Test
    public void testFromFilter_Equal() {
        assertFromFilter(new Filter("cVarchar", "xyz", "=", false, true), "cVarchar = $a", "xyz");
    }

    @Test
    public void testFromFilter_NotEqual() {
        assertFromFilter(new Filter("cVarchar", "xyz", "!=", false, true), "cVarchar <> $a", "xyz");
    }

    @Test
    public void testFromFilter_Like() {
        assertFromFilter(new Filter("cVarchar", "xyz", "like", false, false), "cVarchar likeIgnoreCase 'xyz%'");
    }

    @Test
    public void testFromFilter_In() {
        assertFromFilter(new Filter("cVarchar", asList("xyz", "abc"), "in", false, false), "cVarchar in ($a)", asList("xyz", "abc"));
    }

    @Test
    public void testFromFilter_Greater() {
        assertFromFilter(new Filter("cVarchar", 6, ">", false, false), "cVarchar > $a", 6);
    }

    @Test
    public void testFromFilter_Greater_Null() {
        assertFromFilter(new Filter("cVarchar", null, ">", false, false), "cVarchar > $a", new Object[]{null});
    }

    @Test
    public void testFromFilter_GreaterOrEqual() {
        assertFromFilter(new Filter("cVarchar", 5, ">=", false, false), "cVarchar >= $a", 5);
    }

    @Test
    public void testFromFilter_Less() {
        assertFromFilter(new Filter("cVarchar", 7, "<", false, false), "cVarchar < $a", 7);
    }

    @Test
    public void testFromFilter_LessOrEqual() {
        assertFromFilter(new Filter("cVarchar", "xyz", "<=", false, false), "cVarchar <= $a", "xyz");
    }

    @Test
    public void testFromFilter_Date() {
        assertFromFilter(new Filter("cDate", "2016-03-26", ">", false, false), "cDate > $a", "2016-03-26");
    }
}
