package io.agrest.converter.jsonvalue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NormalizerTest {

	@Test
	public void normalize_Null() {
		assertNull(Normalizer.normalize(null, Integer.class));
		assertNull(Normalizer.normalize(null, Long.class));
	}

	@Test
	public void normalize_NonNumber() {
		Object x = new Object();
		assertSame(x, Normalizer.normalize(x, Integer.class));
		assertSame(x, Normalizer.normalize(x, Long.class));
	}

	@Test
	public void normalize_Long() {
		Long smallLong = 5L;
		assertSame(smallLong, Normalizer.normalize(smallLong, Integer.class));
		assertSame(smallLong, Normalizer.normalize(smallLong, Long.class));

		Long bigLong = Long.valueOf(Integer.MAX_VALUE + 1000);
		assertSame(bigLong, Normalizer.normalize(bigLong, Integer.class));
		assertSame(bigLong, Normalizer.normalize(bigLong, Long.class));
	}

	@Test
	public void normalize_Int() {
		Integer integer = 5;
		Long smallLong = 5L;
		assertSame(integer, Normalizer.normalize(integer, Integer.class));
		assertEquals(smallLong, Normalizer.normalize(integer, Long.class));
	}
}
