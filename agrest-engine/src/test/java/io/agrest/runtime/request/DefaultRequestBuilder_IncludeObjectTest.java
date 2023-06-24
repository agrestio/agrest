package io.agrest.runtime.request;

import io.agrest.AgRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultRequestBuilder_IncludeObjectTest {

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
    public void execute_IncludeObject_Path() {

        Map<String, List<String>> params = Map.of("include", List.of("{\"path\":\"rtt\"}"));
        AgRequest request = builder
                .mergeClientParams(params)
                .build();

        assertEquals(1, request.getIncludes().size());
        assertEquals("rtt", request.getIncludes().get(0).getPath());
    }

    @Test
    public void execute_IncludeObject_MapBy() {

        Map<String, List<String>> params = Map.of("include", List.of("{\"path\":\"rtt\",\"mapBy\":\"rtu\"}"));
        AgRequest request = builder
                .mergeClientParams(params)
                .build();

        assertEquals(1, request.getIncludes().size());
        assertTrue(request.getIncludes().get(0).getPath().equalsIgnoreCase("rtt"));
        assertTrue(request.getIncludes().get(0).getMapBy().equalsIgnoreCase("rtu"));
    }
}
