package io.agrest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SimpleResponseTest {

	@Test
	public void testConstructor() {

		SimpleResponse response = new SimpleResponse(true, "YYYY");

		assertTrue(response.isSuccess());
		assertEquals("YYYY", response.getMessage());
	}

}
