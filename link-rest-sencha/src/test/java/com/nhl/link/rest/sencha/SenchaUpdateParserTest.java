package com.nhl.link.rest.sencha;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SenchaUpdateParserTest {

	private SenchaUpdateParser processor;

	@Before
	public void before() {
		processor = new SenchaUpdateParser(mock(IRelationshipMapper.class), mock(IJacksonService.class),
				mock(IJsonValueConverterFactory.class));
	}

	@Test
	public void testIsTempId() {
		JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

		assertFalse(processor.isTempId(null));
		assertFalse(processor.isTempId(nodeFactory.objectNode()));
		assertTrue(processor.isTempId(nodeFactory.textNode("My-123")));
		assertFalse(processor.isTempId(nodeFactory.textNode("My-My")));
	}

}
