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
import io.agrest.runtime.protocol.IMapByParser;
import io.agrest.runtime.protocol.ISizeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.protocol.IncludeParser;
import io.agrest.runtime.protocol.MapByParser;
import io.agrest.runtime.protocol.SizeParser;
import io.agrest.runtime.protocol.SortParser;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestParserStage_IncludeObjectTest extends TestWithCayenneMapping {

    private ParseRequestStage parseStage;

	@Before
	public void setUp() {

		IJacksonService jacksonService = new JacksonService();

        // prepare parse request stage
        ICayenneExpParser expParser = new CayenneExpParser(jacksonService);
        ISortParser sortParser = new SortParser(jacksonService);
        IMapByParser mapByParser = new MapByParser();
        ISizeParser sizeParser = new SizeParser();
        IIncludeParser includeParser = new IncludeParser(jacksonService, expParser, sortParser, mapByParser, sizeParser);
        IExcludeParser excludeParser = new ExcludeParser(jacksonService);

        this.parseStage = new ParseRequestStage(expParser, sortParser, mapByParser, includeParser, excludeParser);
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
        assertTrue(context.getRawRequest().getIncludes().get(0).getMapBy().getPath().equalsIgnoreCase(E3.E5.getName()));
	}
}
