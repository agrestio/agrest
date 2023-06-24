package io.agrest.runtime.request;

import io.agrest.AgException;
import io.agrest.AgRequest;
import io.agrest.protocol.Direction;
import io.agrest.protocol.Sort;
import io.agrest.exp.parser.ExpRoot;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.runtime.protocol.ExcludeParser;
import io.agrest.runtime.protocol.ExpParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IExpParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISizeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.protocol.IncludeParser;
import io.agrest.runtime.protocol.SizeParser;
import io.agrest.runtime.protocol.SortParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultRequestBuilderTest {

    DefaultRequestBuilder builder;

    @BeforeEach
    public void beforeEach() {

        IJacksonService jacksonService = JacksonService.create();

        // prepare parse request stage
        IExpParser expParser = new ExpParser(jacksonService);
        ISortParser sortParser = new SortParser(jacksonService);
        ISizeParser sizeParser = new SizeParser();
        IIncludeParser includeParser = new IncludeParser(jacksonService, expParser, sortParser, sizeParser);
        IExcludeParser excludeParser = new ExcludeParser(jacksonService);

        this.builder = new DefaultRequestBuilder(expParser, sortParser, includeParser, excludeParser);
    }

    @Test
    public void build_Defaults() {

        AgRequest request = builder.build();

        assertNotNull(request);
        assertNull(request.getExp());
        assertTrue(request.getSorts().isEmpty());
        assertNull(request.getMapBy());
        assertNull(request.getLimit());
        assertNull(request.getStart());
        assertTrue(request.getIncludes().isEmpty());
        assertTrue(request.getExcludes().isEmpty());
    }

    @Test
    public void build_Include() {

        Map<String, List<String>> params = Map.of("include", List.of("a", "b"));

        AgRequest request = builder
                .mergeClientParams(params)
                .build();

        assertEquals(2, request.getIncludes().size());
        assertEquals("a", request.getIncludes().get(0).getPath());
        assertEquals("b", request.getIncludes().get(1).getPath());
    }

    @Test
    public void build_Include_Array() {

        Map<String, List<String>> params = Map.of("include", List.of("[\"a\", \"b\"]"));
        AgRequest request = builder.mergeClientParams(params).build();

        assertEquals(2, request.getIncludes().size());
        assertEquals("a", request.getIncludes().get(0).getPath());
        assertEquals("b", request.getIncludes().get(1).getPath());
    }

    @Test
    public void build_Exclude() {

        Map<String, List<String>> params = Map.of("exclude", List.of("a", "b"));
        AgRequest request = builder.mergeClientParams(params).build();

        assertEquals(2, request.getExcludes().size());
        assertEquals("a", request.getExcludes().get(0).getPath());
        assertEquals("b", request.getExcludes().get(1).getPath());
    }

    @Test
    public void build_Exclude_Array() {

        Map<String, List<String>> params = Map.of("exclude", List.of("[\"a\", \"b\"]"));
        AgRequest request = builder.mergeClientParams(params).build();

        assertEquals(2, request.getExcludes().size());
        assertEquals("a", request.getExcludes().get(0).getPath());
        assertEquals("b", request.getExcludes().get(1).getPath());
    }

    @Test
    public void build_IncludeExclude() {

        Map<String, List<String>> params = Map.of(
                "include", List.of("a", "b", "id"),
                "exclude", List.of("a", "c"));
        AgRequest request = builder.mergeClientParams(params).build();

        assertEquals(3, request.getIncludes().size());
        assertEquals("a", request.getIncludes().get(0).getPath());
        assertEquals("b", request.getIncludes().get(1).getPath());
        assertEquals("id", request.getIncludes().get(2).getPath());

        assertEquals(2, request.getExcludes().size());
        assertEquals("a", request.getExcludes().get(0).getPath());
        assertEquals("c", request.getExcludes().get(1).getPath());
    }

    @Test
    public void build_IncludeRels() {

        Map<String, List<String>> params = Map.of("include", List.of("rtss"));
        AgRequest request = builder.mergeClientParams(params).build();

        assertEquals(1, request.getIncludes().size());
        assertEquals("rtss", request.getIncludes().get(0).getPath());
    }

    @Test
    public void build_SortSimple_NoDir() {

        Map<String, List<String>> params = Map.of("sort", List.of("rtss"));
        AgRequest request = builder.mergeClientParams(params).build();

        assertNotNull(request.getSorts());
        Sort ordering = request.getSorts().get(0);
        assertEquals("rtss", ordering.getPath());
    }

    @Test
    public void build_SortSimple_ASC() {

        Map<String, List<String>> params = Map.of(
                "sort", List.of("rtss"),
                "direction", List.of("ASC"));

        AgRequest request = builder.mergeClientParams(params).build();

        assertNotNull(request.getSorts());
        assertEquals(1, request.getSorts().size());
        Sort ordering = request.getSorts().get(0);
        assertEquals("rtss", ordering.getPath());
        assertEquals(Direction.asc, ordering.getDirection());
    }

    @Test
    public void build_SortSimple_DESC() {

        Map<String, List<String>> params = Map.of(
                "sort", List.of("rtss"),
                "direction", List.of("DESC"));

        AgRequest request = builder.mergeClientParams(params).build();

        assertNotNull(request.getSorts());

        Sort ordering = request.getSorts().get(0);
        assertEquals("rtss", ordering.getPath());
        assertEquals(Direction.desc, ordering.getDirection());
    }

    @Test
    public void build_SortSimple_Garbage() {

        Map<String, List<String>> params = Map.of(
                "sort", List.of("xx"),
                "direction", List.of("XYZ"));

        assertThrows(AgException.class, () -> builder.mergeClientParams(params).build());
    }

    @Test
    public void build_Sort() {

        Map<String, List<String>> params = Map.of(
                "sort", List.of("[{\"path\":\"a\",\"direction\":\"DESC\"},{\"path\":\"b\",\"direction\":\"ASC\"}]"));

        AgRequest request = builder.mergeClientParams(params).build();

        assertNotNull(request.getSorts());
        assertEquals(2, request.getSorts().size());

        Sort o1 = request.getSorts().get(0);
        Sort o2 = request.getSorts().get(1);

        assertEquals("a", o1.getPath());
        assertEquals(Direction.desc, o1.getDirection());
        assertEquals("b", o2.getPath());
        assertEquals(Direction.asc, o2.getDirection());
    }

    @Test
    public void build_Sort_Dupes() {

        Map<String, List<String>> params = Map.of(
                "sort", List.of("[{\"path\":\"a\",\"direction\":\"DESC\"},{\"path\":\"a\",\"direction\":\"ASC\"}]"));

        AgRequest request = builder.mergeClientParams(params).build();

        assertNotNull(request.getSorts());
        assertEquals(2, request.getSorts().size());

        Sort o1 = request.getSorts().get(0);
        Sort o2 = request.getSorts().get(1);

        assertEquals("a", o1.getPath());
        assertEquals(Direction.desc, o1.getDirection());
        assertEquals("a", o2.getPath());
        assertEquals(Direction.asc, o2.getDirection());
    }

    @Test
    public void build_Sort_BadSpec() {
        Map<String, List<String>> params = Map.of("sort", List.of("[{\"path\":\"p1\",\"direction\":\"DESC\"},{\"path\":\"p2\",\"direction\":\"XXX\"}]"));
        assertThrows(AgException.class, () -> builder.mergeClientParams(params).build());
    }

    @Test
    public void build_Exp_BadSpec() {
        Map<String, List<String>> params = Map.of(
                "exp", List.of("{exp : \"numericProp = 12345 and stringProp = 'John Smith' and booleanProp = true\"}"));

        assertThrows(AgException.class, () -> builder.mergeClientParams(params).build());
    }

    @Test
    public void build_Exp() {

        Map<String, List<String>> params = Map.of("exp", List.of("{\"exp\" : \"a = 'John Smith'\"}"));
        AgRequest request = builder.mergeClientParams(params).build();

        assertNotNull(request.getExp());

        assertEquals("a = 'John Smith'", ((ExpRoot) request.getExp()).getTemplate());
    }
}
