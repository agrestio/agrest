package io.agrest.sencha.runtime.processor.select;

import io.agrest.LrRequest;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Sort;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.runtime.processor.select.SelectContext;
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
import io.agrest.sencha.SenchaRequest;
import io.agrest.sencha.runtime.protocol.ISenchaFilterParser;
import io.agrest.sencha.runtime.protocol.SenchaFilterParser;
import io.agrest.unit.TestWithCayenneMapping;
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
