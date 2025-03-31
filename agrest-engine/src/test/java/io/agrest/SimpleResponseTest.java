package io.agrest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleResponseTest {

	@Test
	public void of() {

		SimpleResponse response = SimpleResponse.of(201, "YYYY");

		assertEquals(201, response.getStatus());
		assertEquals("YYYY", response.getMessage());
	}

}
