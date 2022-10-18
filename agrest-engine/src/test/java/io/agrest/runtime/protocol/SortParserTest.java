package io.agrest.runtime.protocol;

import io.agrest.protocol.Direction;
import io.agrest.protocol.Sort;
import io.agrest.runtime.jackson.JacksonService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SortParserTest {

    private static SortParser parser;

    @BeforeAll
    public static void beforeAll() {
        parser = new SortParser(JacksonService.create());
    }

    @Test
    public void testProcess_Array() {

        List<Sort> orderings = parser.parse("[{\"path\":\"name\"}," +
                "{\"path\":\"address\",\"direction\":\"ASC\"}," +
                "{\"path\":\"city\",\"direction\":\"DESC_CI\"}]", null);

        assertNotNull(orderings);
        assertEquals(3, orderings.size());

        assertEquals("name", orderings.get(0).getPath());
        assertEquals(Direction.asc, orderings.get(0).getDirection());

        assertEquals("address", orderings.get(1).getPath());
        assertEquals(Direction.asc, orderings.get(1).getDirection());

        assertEquals("city", orderings.get(2).getPath());
        assertEquals(Direction.desc_ci, orderings.get(2).getDirection());
    }

    @Test
    public void testProcess_Object() {

        List<Sort> orderings = parser.parse("{\"path\":\"name\"}", null);

        assertEquals(1, orderings.size());
        assertEquals("name", orderings.get(0).getPath());
        assertEquals(Direction.asc, orderings.get(0).getDirection());
    }

    @Test
    public void testProcess_Simple() {

        List<Sort> orderings = parser.parse("name", null);

        assertEquals(1, orderings.size());
        assertEquals("name", orderings.get(0).getPath());
        assertEquals(Direction.asc, orderings.get(0).getDirection());
    }

    @Test
    public void testProcess_Simple_ASC() {

        List<Sort> orderings = parser.parse("name", "ASC");

        assertEquals(1, orderings.size());
        assertEquals("name", orderings.get(0).getPath());
        assertEquals(Direction.asc, orderings.get(0).getDirection());
    }

    @Test
    public void testProcess_Simple_DESC() {

        List<Sort> orderings = parser.parse("name", "DESC");

        assertEquals(1, orderings.size());
        assertEquals("name", orderings.get(0).getPath());
        assertEquals(Direction.desc, orderings.get(0).getDirection());
    }

    @Test
    public void testProcess_Simple_ASC_INSENSITIVE() {

        List<Sort> orderings = parser.parse("name", "ASC_CI");

        assertEquals(1, orderings.size());
        assertEquals("name", orderings.get(0).getPath());
        assertEquals(Direction.asc_ci, orderings.get(0).getDirection());
    }

    @Test
    public void testProcess_Simple_DESC_INSENSITIVE() {

        List<Sort> orderings = parser.parse("name", "DESC_CI");

        assertEquals(1, orderings.size());
        assertEquals("name", orderings.get(0).getPath());
        assertEquals(Direction.desc_ci, orderings.get(0).getDirection());
    }
}
