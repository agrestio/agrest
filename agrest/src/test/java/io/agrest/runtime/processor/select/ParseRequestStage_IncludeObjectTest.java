package io.agrest.runtime.processor.select;

import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.runtime.protocol.CayenneExpParser;
import io.agrest.runtime.protocol.ExcludeParser;
import io.agrest.runtime.protocol.ICayenneExpParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISizeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.protocol.IncludeParser;
import io.agrest.runtime.protocol.SizeParser;
import io.agrest.runtime.protocol.SortParser;
import io.agrest.runtime.request.DefaultRequestBuilderFactory;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParseRequestStage_IncludeObjectTest extends TestWithCayenneMapping {

    private ParseRequestStage parseStage;

    @Before
    public void setUp() {

        IJacksonService jacksonService = new JacksonService();

        // prepare parse request stage
        ICayenneExpParser expParser = new CayenneExpParser(jacksonService);
        ISortParser sortParser = new SortParser(jacksonService);
        ISizeParser sizeParser = new SizeParser();
        IIncludeParser includeParser = new IncludeParser(jacksonService, expParser, sortParser, sizeParser);
        IExcludeParser excludeParser = new ExcludeParser(jacksonService);

        IAgRequestBuilderFactory requestBuilderFactory
                = new DefaultRequestBuilderFactory(expParser, sortParser, includeParser, excludeParser);
        this.parseStage = new ParseRequestStage(requestBuilderFactory);
    }

    @Test
    public void testToDataRequest_IncludeObject_Path() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("include")).thenReturn(Arrays.asList("{\"path\":\"e3s\"}"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.execute(context);

        assertNotNull(context.getRawRequest());

        assertEquals(1, context.getRawRequest().getIncludes().size());
        assertEquals("e3s", context.getRawRequest().getIncludes().get(0).getPath());
    }

    @Test
    public void testToDataRequest_IncludeObject_MapBy() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("include")).thenReturn(Arrays.asList("{\"path\":\"e3s\",\"mapBy\":\"e5\"}"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.execute(context);
        assertNotNull(context.getRawRequest());

        assertEquals(1, context.getRawRequest().getIncludes().size());
        assertTrue(context.getRawRequest().getIncludes().get(0).getPath().equalsIgnoreCase(E2.E3S.getName()));
        assertTrue(context.getRawRequest().getIncludes().get(0).getMapBy().equalsIgnoreCase(E3.E5.getName()));
    }
}
