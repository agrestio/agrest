package io.agrest.sencha.runtime.entity;

import io.agrest.AgException;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.meta.AgEntity;
import io.agrest.runtime.cayenne.converter.CayenneExpressionConverter;
import io.agrest.runtime.entity.ExpressionPostProcessor;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.runtime.path.PathDescriptorManager;
import io.agrest.sencha.protocol.Filter;
import io.agrest.unit.TestWithCayenneMapping;
import org.apache.cayenne.exp.Expression;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.GregorianCalendar;

import static java.util.Arrays.asList;
import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SenchaFilterExpressionCompilerTest extends TestWithCayenneMapping {

    private AgEntity<E4> e4Entity;
    private SenchaFilterExpressionCompiler processor;

    @Before
    public void before() {

        IJacksonService jsonParser = new JacksonService();
        PathDescriptorManager pathDescriptorManager = new PathDescriptorManager();

        this.processor = new SenchaFilterExpressionCompiler(pathDescriptorManager, new ExpressionPostProcessor(pathDescriptorManager));
        this.e4Entity = getAgEntity(E4.class);
    }

    private Expression process(Filter... filters) {
        return new CayenneExpressionConverter().apply(processor.process(e4Entity, Arrays.asList(filters)));
    }

    private void assertProcess(String expectedExpression, Filter... filters) {
        assertEquals(exp(expectedExpression), process(filters));
    }

    private void assertProcess(Expression expectedExpression, Filter... filters) {
        assertEquals(expectedExpression, process(filters));
    }

    @Test
    public void testProcess_SingleFilter() {
        assertProcess("cVarchar likeIgnoreCase 'xyz%'", new Filter("cVarchar", "xyz", "like", false, false));
    }

    @Test
    public void testProcess_SingleFilter_Disabled() {
        assertNull(process(new Filter("cVarchar", "xyz", "like", true, false)));
    }

    @Test
    public void testProcess_MultipleFilters() {
        assertProcess("cVarchar likeIgnoreCase 'xyz%' and cVarchar likeIgnoreCase '123%'",
                new Filter("cVarchar", "xyz", "like", false, false),
                new Filter("cVarchar", "123", "like", false, false));
    }

    @Test
    public void testProcess_MultipleFilters_Disabled() {
        assertProcess("cVarchar likeIgnoreCase '123%'",
                new Filter("cVarchar", "xyz", "like", true, false),
                new Filter("cVarchar", "123", "like", false, false));
    }

    @Test(expected = AgException.class)
    public void testProcess_InvalidProperty() {
        assertNull(process(new Filter("cDummp", "xyz", "like", false, false)));
    }

    @Test
    public void testProcess_ValueEscape() {
        assertProcess(E4.C_VARCHAR.likeIgnoreCase("x\\_\\%%"), new Filter("cVarchar", "x_%", "like", false, false));
    }

    @Test
    public void testProcess_ValueNull() {
        assertProcess(E4.C_VARCHAR.isNull(), new Filter("cVarchar", null, "like", false, false));
    }

    @Test
    public void testProcess_ExactMatch() {
        assertProcess("cVarchar = 'xyz'", new Filter("cVarchar", "xyz", "like", false, true));
    }

    @Test
    public void testProcess_Equal() {
        assertProcess("cVarchar = 'xyz'", new Filter("cVarchar", "xyz", "=", false, true));
    }

    @Test
    public void testProcess_NotEqual() {
        assertProcess("cVarchar != 'xyz'", new Filter("cVarchar", "xyz", "!=", false, true));
    }

    @Test
    public void testProcess_Like() {
        assertProcess("cVarchar likeIgnoreCase 'xyz%'", new Filter("cVarchar", "xyz", "like", false, false));
    }

    @Test
    public void testProcess_In() {
        assertProcess("cVarchar in ('xyz', 'abc')", new Filter("cVarchar", asList("xyz", "abc"), "in", false, false));
    }

    @Test
    public void testProcess_Greater() {
        assertProcess("cVarchar > 6", new Filter("cVarchar", 6, ">", false, false));
    }

    @Test
    public void testProcess_Greater_Null() {
        assertProcess("false", new Filter("cVarchar", null, ">", false, false));
    }

    @Test
    public void testProcess_GreaterOrEqual() {
        assertProcess("cVarchar >= 5", new Filter("cVarchar", 5, ">=", false, false));
    }

    @Test
    public void testProcess_Less() {
        assertProcess("cVarchar < 7", new Filter("cVarchar", 7, "<", false, false));
    }

    @Test
    public void testProcess_LessOrEqual() {
        assertProcess("cVarchar <= 'xyz'", new Filter("cVarchar", "xyz", "<=", false, false));
    }

    @Test
    public void testProcess_Date() {
        assertProcess(exp("cDate > $d", new GregorianCalendar(2016, 2, 26).getTime()),
                new Filter("cDate", "2016-03-26", ">", false, false));
    }
}
