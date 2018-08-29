package com.nhl.link.rest.sencha.runtime.processor.select;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Sort;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.protocol.CayenneExpParser;
import com.nhl.link.rest.runtime.protocol.ICayenneExpParser;
import com.nhl.link.rest.runtime.protocol.IMapByParser;
import com.nhl.link.rest.runtime.protocol.MapByParser;
import com.nhl.link.rest.runtime.protocol.ISizeParser;
import com.nhl.link.rest.runtime.protocol.SizeParser;
import com.nhl.link.rest.runtime.protocol.ISortParser;
import com.nhl.link.rest.runtime.protocol.SortParser;
import com.nhl.link.rest.runtime.protocol.ExcludeParser;
import com.nhl.link.rest.runtime.protocol.IExcludeParser;
import com.nhl.link.rest.runtime.protocol.IIncludeParser;
import com.nhl.link.rest.runtime.protocol.IncludeParser;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.sencha.SenchaRequest;
import com.nhl.link.rest.sencha.runtime.protocol.ISenchaFilterParser;
import com.nhl.link.rest.sencha.runtime.protocol.SenchaFilterParser;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SenchaParseRequestStageTest extends TestWithCayenneMapping {

    private SenchaParseRequestStage parseStage;

	@Before
	public void before() {

		IJacksonService jacksonService = new JacksonService();

		// prepare parse request stage
        ICayenneExpParser expParser = new CayenneExpParser(jacksonService);
		ISortParser sortParser = new SortParser(jacksonService);
        IMapByParser mapByParser = new MapByParser();
        ISizeParser sizeParser = new SizeParser();
        IIncludeParser includeParser = new IncludeParser(jacksonService, expParser, sortParser, mapByParser, sizeParser);
        IExcludeParser excludeParser = new ExcludeParser(jacksonService);
        ISenchaFilterParser filterParser = new SenchaFilterParser(jacksonService);

        this.parseStage = new SenchaParseRequestStage(expParser, sortParser, mapByParser, includeParser, excludeParser, filterParser);

	}

    @Test
	public void testSelectRequest_Filter() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get(SenchaParseRequestStage.FILTER)).thenReturn(Collections.singletonList("[{\"property\":\"name\",\"value\":\"xyz\"}]"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.doExecute(context);

        SenchaRequest senchaRequest = SenchaRequest.get(context);

		assertNotNull(senchaRequest);
		assertFalse(senchaRequest.getFilters().isEmpty());
		assertEquals("name", senchaRequest.getFilters().get(0).getProperty());
		assertEquals("xyz", senchaRequest.getFilters().get(0).getValue());
		assertEquals("like", senchaRequest.getFilters().get(0).getOperator());
	}


	@Test
	public void testSelectRequest_Filter_CayenneExp() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("cayenneExp")).thenReturn(Collections.singletonList("{\"exp\" : \"address = '1 Main Street'\"}"));
		when(params.get(SenchaParseRequestStage.FILTER)).thenReturn(Collections.singletonList("[{\"property\":\"name\",\"value\":\"xyz\"}]"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.doExecute(context);

		SenchaRequest senchaRequest = SenchaRequest.get(context);

		assertNotNull(senchaRequest);
		assertFalse(senchaRequest.getFilters().isEmpty());
		assertEquals("name", senchaRequest.getFilters().get(0).getProperty());
		assertEquals("xyz", senchaRequest.getFilters().get(0).getValue());
		assertEquals("like", senchaRequest.getFilters().get(0).getOperator());

		LrRequest lrRequest = context.getRawRequest();

		assertNotNull(lrRequest.getCayenneExp());
		assertEquals("address = '1 Main Street'", lrRequest.getCayenneExp().getExp());
	}

	@Test
	public void testSelectRequest_Sort_Group() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(
				Collections.singletonList("[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]"));
		when(params.get(SenchaParseRequestStage.GROUP)).thenReturn(
				Collections.singletonList("[{\"property\":\"id\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]"));


        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.doExecute(context);

		SenchaRequest senchaRequest = SenchaRequest.get(context);

		assertNotNull(senchaRequest);
		assertNotNull(senchaRequest.getGroup());
		assertEquals(2, senchaRequest.getGroup().getSorts().size());
		assertEquals("id", senchaRequest.getGroup().getSorts().get(0).getProperty());
		assertEquals(Dir.DESC, senchaRequest.getGroup().getSorts().get(0).getDirection());
		assertEquals("address", senchaRequest.getGroup().getSorts().get(1).getProperty());
		assertEquals(Dir.ASC, senchaRequest.getGroup().getSorts().get(1).getDirection());


		Sort sort = context.getRawRequest().getSort();

		assertNotNull(sort);
		assertEquals(2, sort.getSorts().size());
		assertEquals("name", sort.getSorts().get(0).getProperty());
		assertEquals(Dir.DESC, sort.getSorts().get(0).getDirection());
		assertEquals("address", sort.getSorts().get(1).getProperty());
		assertEquals(Dir.ASC, sort.getSorts().get(1).getDirection());
	}
}
