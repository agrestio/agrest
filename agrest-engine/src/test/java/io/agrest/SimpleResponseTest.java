package io.agrest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleResponseTest {

	@Test
	public void testOf() {

		SimpleResponse response = SimpleResponse.of(201, true, "YYYY");

		assertEquals(201, response.getStatus());
		assertTrue(response.isSuccess());
		assertEquals("YYYY", response.getMessage());
	}

}
