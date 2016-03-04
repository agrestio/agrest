package com.nhl.link.rest.runtime.adapter.sencha;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

public class SenchaUpdateParserTest {

	private SenchaUpdateParser processor;

	@Before
	public void before() {
		processor = new SenchaUpdateParser(mock(IRelationshipMapper.class), mock(IJsonValueConverterFactory.class),
				mock(IJacksonService.class));
	}

	@Test
	public void testIsTempId() {

		assertFalse(processor.isTempId(null));
		assertFalse(processor.isTempId(new Object()));
		assertTrue(processor.isTempId("My-123"));
		assertFalse(processor.isTempId("My-My"));
	}

}
