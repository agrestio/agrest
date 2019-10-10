package io.agrest.runtime.protocol;

import io.agrest.protocol.Dir;
import io.agrest.protocol.Sort;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SortParserTest extends TestWithCayenneMapping {

    private SortParser parser;

    @Before
    public void before() {
        JacksonService jacksonService = new JacksonService();
        this.parser = new SortParser(jacksonService);
    }

    @Test
    public void testProcess_Array() {

        List<Sort> orderings = parser.parse("[{\"property\":\"name\"}," +
                "{\"property\":\"address\",\"direction\":\"ASC\"}," +
                "{\"property\":\"city\",\"direction\":\"DESC_INSENSITIVE\"}]", null);

        assertNotNull(orderings);
        assertEquals(3, orderings.size());

        assertEquals("name", orderings.get(0).getProperty());
        assertEquals(Dir.ASC, orderings.get(0).getDirection());

        assertEquals("address", orderings.get(1).getProperty());
        assertEquals(Dir.ASC, orderings.get(1).getDirection());

        assertEquals("city", orderings.get(2).getProperty());
        assertEquals(Dir.DESC_CI, orderings.get(2).getDirection());
    }

    @Test
    public void testProcess_Object() {

        List<Sort> orderings = parser.parse("{\"property\":\"name\"}", null);

        assertEquals(1, orderings.size());
        assertEquals("name", orderings.get(0).getProperty());
        assertEquals(Dir.ASC, orderings.get(0).getDirection());
    }

    @Test
    public void testProcess_Simple() {

        List<Sort> orderings = parser.parse("name", null);

        assertEquals(1, orderings.size());
        assertEquals("name", orderings.get(0).getProperty());
        assertEquals(Dir.ASC, orderings.get(0).getDirection());
    }

    @Test
    public void testProcess_Simple_ASC() {

        List<Sort> orderings = parser.parse("name", "ASC");

        assertEquals(1, orderings.size());
        assertEquals("name", orderings.get(0).getProperty());
        assertEquals(Dir.ASC, orderings.get(0).getDirection());
    }

    @Test
    public void testProcess_Simple_DESC() {

        List<Sort> orderings = parser.parse("name", "DESC");

        assertEquals(1, orderings.size());
        assertEquals("name", orderings.get(0).getProperty());
        assertEquals(Dir.DESC, orderings.get(0).getDirection());
    }

    @Test
    public void testProcess_Simple_ASC_INSENSITIVE() {

        List<Sort> orderings = parser.parse("name", "ASC_INSENSITIVE");

        assertEquals(1, orderings.size());
        assertEquals("name", orderings.get(0).getProperty());
        assertEquals(Dir.ASC_CI, orderings.get(0).getDirection());
    }

    @Test
    public void testProcess_Simple_DESC_INSENSITIVE() {

        List<Sort> orderings = parser.parse("name", "DESC_INSENSITIVE");

        assertEquals(1, orderings.size());
        assertEquals("name", orderings.get(0).getProperty());
        assertEquals(Dir.DESC_CI, orderings.get(0).getDirection());
    }
}
