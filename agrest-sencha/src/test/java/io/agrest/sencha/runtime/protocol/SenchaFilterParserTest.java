package io.agrest.sencha.runtime.protocol;

import io.agrest.runtime.jackson.JacksonService;
import io.agrest.sencha.protocol.Filter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SenchaFilterParserTest {

    final SenchaFilterParser parser = new SenchaFilterParser(new JacksonService());

    private void assertFilter(
            String expectedProperty,
            Object expectedValue,
            String expectedOperator,
            boolean expectedDisabled,
            boolean expectedExectMatch,
            Filter filter) {

        assertEquals(expectedProperty, filter.getProperty());
        assertEquals(expectedValue, filter.getValue());
        assertEquals(expectedOperator, filter.getOperator());
        assertEquals(expectedDisabled, filter.isDisabled());
        assertEquals(expectedExectMatch, filter.isExactMatch());
    }

    @Test
    public void testFromString_SingleFilter() {
        List<Filter> filters = parser.fromString("[{\"property\":\"cVarchar\",\"value\":\"xyz\"}]");
        assertEquals(1, filters.size());
        assertFilter("cVarchar", "xyz", "like", false, false, filters.get(0));
    }

    @Test
    public void testFromString_SingleFilter_Disabled() {
        List<Filter> filters = parser.fromString("[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"disabled\":\"true\"}]");
        assertEquals(1, filters.size());
        assertFilter("cVarchar", "xyz", "like", true, false, filters.get(0));
    }

    @Test
    public void testFromString_MultipleFilters() {
        List<Filter> filters = parser
                .fromString("[{\"property\":\"cVarchar\",\"value\":\"xyz\"}, {\"property\":\"cVarchar\",\"value\":\"123\"}]");

        assertEquals(2, filters.size());
        assertFilter("cVarchar", "xyz", "like", false, false, filters.get(0));
        assertFilter("cVarchar", "123", "like", false, false, filters.get(1));
    }

    @Test
    public void testFromString_MultipleFilters_Disabled() {
        List<Filter> filters = parser
                .fromString("[{\"property\":\"cVarchar\",\"value\":\"xyz\", \"disabled\":\"true\"}, {\"property\":\"cVarchar\",\"value\":\"123\"}]");

        assertEquals(2, filters.size());
        assertFilter("cVarchar", "xyz", "like", true, false, filters.get(0));
        assertFilter("cVarchar", "123", "like", false, false, filters.get(1));
    }

    @Test
    public void testFromString_ValueNull() {
        List<Filter> filters = parser.fromString("[{\"property\":\"cVarchar\",\"value\":null}]");
        assertEquals(1, filters.size());
        assertFilter("cVarchar", null, "like", false, false, filters.get(0));
    }

    @Test
    public void testFromString_ExactMatch() {
        List<Filter> filters = parser.fromString("[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"exactMatch\":true}]");
        assertEquals(1, filters.size());
        assertFilter("cVarchar", "xyz", "like", false, true, filters.get(0));
    }

    @Test
    public void testFromString_Equal() {
        List<Filter> filters = parser.fromString("[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"operator\":\"=\"}]");
        assertEquals(1, filters.size());
        assertFilter("cVarchar", "xyz", "=", false, false, filters.get(0));
    }

    @Test
    public void testFromString_In() {
        List<Filter> filters = parser.fromString("[{\"property\":\"cVarchar\",\"value\":[\"xyz\",\"abc\"],\"operator\":\"in\"}]");
        assertEquals(1, filters.size());
        assertFilter("cVarchar", asList("xyz", "abc"), "in", false, false, filters.get(0));
    }

    @Test
    public void testFromString_NumericVal() {
        List<Filter> filters = parser.fromString("[{\"property\":\"cInt\",\"value\":6,\"operator\":\">\"}]");

        assertEquals(1, filters.size());
        assertFilter("cInt", 6, ">", false, false, filters.get(0));
    }

    @Test
    public void testFromString_DateVal() {
        List<Filter> filters = parser.fromString("[{\"property\":\"cDate\",\"value\":\"2016-03-26\",\"operator\":\">\"}]");

        assertEquals(1, filters.size());
        assertFilter("cDate", "2016-03-26", ">", false, false, filters.get(0));
    }
}
