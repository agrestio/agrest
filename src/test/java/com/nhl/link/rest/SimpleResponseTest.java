package com.nhl.link.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.nhl.link.rest.SimpleResponse;

public class SimpleResponseTest {

	@Test
	public void testConstructor() {

		SimpleResponse response = new SimpleResponse(true, "YYYY");

		assertTrue(response.isSuccess());
		assertEquals("YYYY", response.getMessage());
	}

}
