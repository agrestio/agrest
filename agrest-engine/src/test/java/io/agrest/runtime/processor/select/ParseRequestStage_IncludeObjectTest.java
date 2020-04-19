package io.agrest.runtime.processor.select;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
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
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParseRequestStage_IncludeObjectTest {

    private static ParseRequestStage stage;

    @BeforeClass
    public static void beforeAll() {

        IJacksonService jacksonService = new JacksonService();

        // prepare parse request stage
        ICayenneExpParser expParser = new CayenneExpParser(jacksonService);
        ISortParser sortParser = new SortParser(jacksonService);
        ISizeParser sizeParser = new SizeParser();
        IIncludeParser includeParser = new IncludeParser(jacksonService, expParser, sortParser, sizeParser);
        IExcludeParser excludeParser = new ExcludeParser(jacksonService);

        IAgRequestBuilderFactory requestBuilderFactory
                = new DefaultRequestBuilderFactory(expParser, sortParser, includeParser, excludeParser);
        stage = new ParseRequestStage(requestBuilderFactory);
    }

    @Test
    public void testExecute_IncludeObject_Path() {

        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("include", "{\"path\":\"rtt\"}");
        SelectContext<Ts> context = prepareContext(Ts.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertEquals(1, context.getMergedRequest().getIncludes().size());
        assertEquals("rtt", context.getMergedRequest().getIncludes().get(0).getPath());
    }

    @Test
    public void testExecute_IncludeObject_MapBy() {

        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("include", "{\"path\":\"rtt\",\"mapBy\":\"rtu\"}");
        SelectContext<Ts> context = prepareContext(Ts.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertEquals(1, context.getMergedRequest().getIncludes().size());
        assertTrue(context.getMergedRequest().getIncludes().get(0).getPath().equalsIgnoreCase("rtt"));
        assertTrue(context.getMergedRequest().getIncludes().get(0).getMapBy().equalsIgnoreCase("rtu"));
    }

    protected <T> SelectContext<T> prepareContext(Class<T> type, MultivaluedMap<String, String> params) {
        SelectContext<T> context = new SelectContext<>(type);

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getQueryParameters()).thenReturn(params);

        context.setUriInfo(uriInfo);
        return context;
    }

    public static class Ts {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getN() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getM() {
            throw new UnsupportedOperationException();
        }


        @AgRelationship
        public Tt getRtt() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Tt {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }


        @AgAttribute
        public String getO() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getP() {
            throw new UnsupportedOperationException();
        }

        @AgRelationship
        public Tu getRtu() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Tu {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }
    }
}
