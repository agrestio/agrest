package com.nhl.link.rest.runtime.parser;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.parser.RequestJsonParser;

public class RequestJsonParserTest {

	private JsonFactory jsonFactory;

	@Before
	public void setUp() {
		this.jsonFactory = new ObjectMapper().getJsonFactory();
	}

	@Test
	public void testParseJSONStringArray() {
		RequestJsonParser parser = new RequestJsonParser(jsonFactory);

		String[] a0 = parser.parseJSONStringArray("", new ObjectMapper());
		assertArrayEquals(new String[] {}, a0);

		String[] a1 = parser.parseJSONStringArray("\"quoted\"", new ObjectMapper());
		assertArrayEquals(new String[] { "quoted" }, a1);

		String[] a2 = parser.parseJSONStringArray("[\"s1\",\"s2\"]", new ObjectMapper());
		assertArrayEquals(new String[] { "s1", "s2" }, a2);

		String[] a3 = parser.parseJSONStringArray("  [\"s1\",\"s2\"]", new ObjectMapper());
		assertArrayEquals(new String[] { "s1", "s2" }, a3);

		String[] a5 = parser.parseJSONStringArray(null, new ObjectMapper());
		assertArrayEquals(new String[] {}, a5);
	}

	@Test(expected = LinkRestException.class)
	public void testParseJSONString_BadJSON() {
		RequestJsonParser parser = new RequestJsonParser(jsonFactory);
		parser.parseJSONStringArray("unquoted", new ObjectMapper());
	}

}
