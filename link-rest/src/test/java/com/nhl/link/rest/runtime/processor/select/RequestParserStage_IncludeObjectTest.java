package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.protocol.CayenneExpParser;
import com.nhl.link.rest.runtime.protocol.ExcludeParser;
import com.nhl.link.rest.runtime.protocol.ICayenneExpParser;
import com.nhl.link.rest.runtime.protocol.IExcludeParser;
import com.nhl.link.rest.runtime.protocol.IIncludeParser;
import com.nhl.link.rest.runtime.protocol.IMapByParser;
import com.nhl.link.rest.runtime.protocol.ISizeParser;
import com.nhl.link.rest.runtime.protocol.ISortParser;
import com.nhl.link.rest.runtime.protocol.IncludeParser;
import com.nhl.link.rest.runtime.protocol.MapByParser;
import com.nhl.link.rest.runtime.protocol.SizeParser;
import com.nhl.link.rest.runtime.protocol.SortParser;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
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
